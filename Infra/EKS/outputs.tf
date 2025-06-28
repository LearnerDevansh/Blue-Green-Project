output "cluster_name" {
    value = var.cluster_name
}

output "cluster_endpoint" {
    value = aws_eks_cluster.bankapp.endpoint
}