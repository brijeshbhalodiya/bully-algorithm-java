# command to build docker image
docker build -t brijeshbhalodiya/bullyalgorithm .

# command to start docker file change Priority, join cluster, cluster_node_ip according to requirement, name
1. docker run --rm --env PRIORITY=2 --env JOINCLUSTER=false --name bully brijeshbhalodiya/bullyalgorithm:latest
2. docker run --rm --env PRIORITY=3 --env JOINCLUSTER=true --env CLUSTER_NODE_IP=172.17.0.2 --name bully-1 brijeshbhalodiya/bullyalgorithm:latest
