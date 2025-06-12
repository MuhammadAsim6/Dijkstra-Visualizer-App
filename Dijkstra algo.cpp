#include <iostream>
#include <vector>
#include <limits>
#include <algorithm>

using namespace std;

const int INF = std::numeric_limits<int>::max();

// Custom Priority Queue (non-heap based, using a sorted vector)
struct NodeDistance {
    int node;
    int distance;

    // For sorting
    bool operator>(const NodeDistance& other) const {
        return distance > other.distance;
    }
};

class CustomPriorityQueue {
private:
    std::vector<NodeDistance> data;

public:
    void push(NodeDistance nd) {
        data.push_back(nd);
        // Keep the vector sorted so smallest distance is at the front
        std::sort(data.begin(), data.end(), std::greater<NodeDistance>());
    }

    NodeDistance pop() {
        if (empty()) {
            // Handle error or return a default/invalid value
             return {-1, -1}; // Example invalid value
        }
        NodeDistance min_node = data.front();
        data.erase(data.begin());
        return min_node;
    }

    bool empty() const {
        return data.empty();
    }
};

// Dijkstra's algorithm implementation
std::vector<int> dijkstra(const std::vector<std::vector<int>>& graph, int start_node) {
    int num_nodes = graph.size();
    std::vector<int> dist(num_nodes, INF);
    dist[start_node] = 0;

    CustomPriorityQueue pq;
    pq.push({start_node, 0});

    while (!pq.empty()) {
        NodeDistance current = pq.pop();
        int u = current.node;
        int d = current.distance;

        // If the extracted distance is greater than the current known distance,
        // we've found a shorter path already, so skip processing this one.
        if (d > dist[u]) {
            continue;
        }

        // Explore neighbors
        for (int v = 0; v < num_nodes; ++v) {
            // If there is an edge from u to v
            if (graph[u][v] != 0 && graph[u][v] != INF) {
                if (dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    pq.push({v, dist[v]});
                }
            }
        }
    }

    return dist;
}

int main() {
    // Example Usage:
    // Graph represented by adjacency matrix
    // 0 --1--> 1 --2--> 2
    // |        ^
    // 4        |
    // |        |
    // v        3
    // 3 -------|
    std::vector<std::vector<int>> graph = {
        {0, 1, 0, 4},
        {0, 0, 2, 0},
        {0, 0, 0, 3},
        {0, 0, 0, 0}
    };
    int start_node = 0;

    std::vector<int> shortest_distances = dijkstra(graph, start_node);

    std::cout << "Shortest distances from node " << start_node << ":\n";
    for (int i = 0; i < shortest_distances.size(); ++i) {
        if (shortest_distances[i] == INF) {
            std::cout << "Node " << i << ": INF\n";
        } else {
            std::cout << "Node " << i << ": " << shortest_distances[i] << "\n";
        }
    }

    return 0;
} 