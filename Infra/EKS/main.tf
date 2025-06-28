#VPC
resource "aws_vpc" "bankapp_vpc" {
  cidr_block = "10.0.0.0/16"
  enable_dns_support = true
  enable_dns_hostnames = true
  tags = {
    Name = "bankapp-vpc"
  }
}

#Internet Gateway
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.bankapp_vpc.id
  tags = {
    Name = "bankapp-igw"
  }
}

#Elastic IP for NAT
resource "aws_eip" "nat_eip" {
  depends_on = [ aws_subnet.public ]
}

#NAT Gateway
resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat_eip.id
  subnet_id = aws_subnet.public[0].id
  tags = {
    Name = "bankapp-nat"
  }
}

#Public Subnet
resource "aws_subnet" "public" {
  count = 2
  vpc_id = aws_vpc.bankapp_vpc.id
  cidr_block = cidrsubnet(aws_vpc.bankapp_vpc.cidr_block, 8, count.index)
  availability_zone = element(["us-east-1a", "us-east-1b"], count.index)
  map_public_ip_on_launch = true
  tags = {
    Name = "bankapp-public-${count.index}"
  }
}

#Private Subnet
resource "aws_subnet" "private" {
  count = 2
  vpc_id = aws_vpc.bankapp_vpc.id 
  cidr_block = cidrsubnet(aws_vpc.bankapp_vpc.cidr_block, 8, count.index + 2)
  availability_zone = element(["us-east-1a", "us-east-1b"], count.index)
  tags = {
    Name = "bankapp-private-${count.index}"
  }   
}

#Public Route Table
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.bankapp_vpc.id
  tags = {
    Name = "bankapp-public-rt"
  }
}

resource "aws_route" "public_internet_access" {
  route_table_id = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = aws_internet_gateway.igw.id
}

#Private Route Table
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.bankapp_vpc.id
  tags = {
    Name = "bankapp-private-rt"
  }
}

resource "aws_route" "private_nat_access" {
  route_table_id = aws_route_table.private.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id = aws_nat_gateway.nat.id  
}

resource "aws_route_table_association" "private_association" {
  count = length(aws_subnet.private)
  subnet_id = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id 
}

#EKS IAM Role
resource "aws_iam_role" "eks_cluster_role" {
    name = "bankapp-eks-cluster-role"

    assume_role_policy = jsonencode({
      Version = "2012-10-17",
      Statement = [
        {
            Action = "sts:AssumeRole",
            Effect = "Allow",
            Principal = {
               Service = "eks.amazonaws.com"
            }
        }
      ]
    })
}

#EKS Cluster Policy
resource "aws_iam_role_policy_attachment" "eks_cluster_AmazonEKSClusterPolicy" {
    policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
    role = aws_iam_role.eks_cluster_role.name
}

#EKS Cluster
resource "aws_eks_cluster" "bankapp" {
    name = "bankapp-eks"
    role_arn = aws_iam_role.eks_cluster_role.arn

    vpc_config {
      subnet_ids = aws_subnet.private[*].id
    }

    depends_on = [ aws_iam_role_policy_attachment.eks_cluster_AmazonEKSClusterPolicy ]
}

#Outputs
output "vpc_id" {
  value = aws_vpc.bankapp_vpc.id
}

output "public_subnet" {
  value = aws_subnet.public[*].id 
}

output "private_subnet" {
  value = aws_subnet.private[*].id
}