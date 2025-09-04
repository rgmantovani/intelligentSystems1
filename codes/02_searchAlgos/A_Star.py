import heapq

class Node:
    def __init__(self, row, col, g_cost, h_cost, parent=None):
        self.row = row
        self.col = col
        self.g_cost = g_cost
        self.h_cost = h_cost
        self.f_cost = g_cost + h_cost
        self.parent = parent

    def __lt__(self, other): # For heapq comparison
        return self.f_cost < other.f_cost

def heuristic(node, goal_node):
    # Example: Manhattan distance
    return abs(node.row - goal_node.row) + abs(node.col - goal_node.col)

def a_star_search(matrix, start, goal):
    rows, cols = len(matrix), len(matrix[0])
    
    start_node = Node(start[0], start[1], 0, heuristic(Node(start[0], start[1], 0, 0), Node(goal[0], goal[1], 0, 0)))
    goal_node = Node(goal[0], goal[1], 0, 0) # h_cost not needed for goal node itself

    open_list = [start_node]
    closed_set = set() # Store (row, col) tuples for quick lookup

    # Dictionary to store the actual node objects for each (row, col)
    # This is important to update g_cost if a better path is found
    node_map = {(start[0], start[1]): start_node}

    while open_list:
        current_node = heapq.heappop(open_list)

        if (current_node.row, current_node.col) == (goal_node.row, goal_node.col):
            # Path found, reconstruct and return
            path = []
            temp = current_node
            while temp:
                path.append((temp.row, temp.col))
                temp = temp.parent
            return path[::-1] # Reverse to get path from start to goal

        closed_set.add((current_node.row, current_node.col))

        # Define possible movements (up, down, left, right, diagonals if allowed)
        # Example: 4-directional movement
        movements = [(0, 1), (0, -1), (1, 0), (-1, 0)] 

        for dr, dc in movements:
            neighbor_row, neighbor_col = current_node.row + dr, current_node.col + dc

            # Check if neighbor is valid (within bounds and not an obstacle)
            if 0 <= neighbor_row < rows and 0 <= neighbor_col < cols and matrix[neighbor_row][neighbor_col] != 1: # Assuming 1 is an obstacle
                if (neighbor_row, neighbor_col) in closed_set:
                    continue

                temp_g_cost = current_node.g_cost + 1 # Assuming cost of 1 per step

                neighbor_key = (neighbor_row, neighbor_col)
                if neighbor_key not in node_map or temp_g_cost < node_map[neighbor_key].g_cost:
                    if neighbor_key not in node_map:
                        neighbor_node = Node(neighbor_row, neighbor_col, temp_g_cost, 
                                             heuristic(Node(neighbor_row, neighbor_col, 0, 0), goal_node), current_node)
                        node_map[neighbor_key] = neighbor_node
                        heapq.heappush(open_list, neighbor_node)
                    else:
                        neighbor_node = node_map[neighbor_key]
                        neighbor_node.g_cost = temp_g_cost
                        neighbor_node.f_cost = neighbor_node.g_cost + neighbor_node.h_cost
                        neighbor_node.parent = current_node
                        # Re-heapify or update in priority queue (more complex with standard heapq)
                        # For simplicity, if already in open_list with worse g_cost, just let the old one be processed later
                        # or use a more sophisticated priority queue implementation.
                        # For now, if it's already in the open list with a higher g_cost, a new node with the better path will be pushed.

    return None # No path found

# Example Usage:
grid = [
    [0, 0, 0, 0, 0],
    [0, 1, 0, 1, 0],
    [0, 0, 0, 0, 0],
    [0, 1, 1, 1, 0],
    [0, 0, 0, 0, 0]
]
start_point = (0, 0)
end_point = (4, 4)

path = a_star_search(grid, start_point, end_point)
if path:
    print("Path found:", path)
else:
    print("No path found.")