# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------

import heapq

# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------

def beam_search(graph, start, goal, beam_width):
    # Each entry in the beam: (total_cost, current_node, path_taken)
    beam = [(0, start, [start])]
    
    while beam:
        new_candidates = []
        
        # Expand all current nodes in the beam
        for cost, current_node, path in beam:
            if current_node == goal:
                return path, cost
            
            # Explore neighbors
            for neighbor, weight in graph.get(current_node, []).items():
                if neighbor not in path:
                    new_path = path + [neighbor]
                    new_cost = cost + weight
                    new_candidates.append((new_cost, neighbor, new_path))
        
        # Sort candidates by cost and prune to keep only the best 'beam_width' paths
        beam = heapq.nsmallest(beam_width, new_candidates, key=lambda x: x[0])
    
    return None, float('inf')

# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------

# Example Graph (Adjacency List with weights)
graph = {
    'A': {'B': 1, 'C': 3},
    'B': {'D': 2, 'E': 4},
    'C': {'F': 1},
    'D': {'G': 1},
    'E': {'G': 1},
    'F': {'G': 5}
}

path, cost = beam_search(graph, 'A', 'G', beam_width=2)
print(f"Path: {path}, Cost: {cost}")
