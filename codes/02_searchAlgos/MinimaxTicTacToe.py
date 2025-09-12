# --------------------------------------------------------------
# Initialize the game board
# --------------------------------------------------------------
board = [[' ' for _ in range(3)] for _ in range(3)]

# --------------------------------------------------------------
# --------------------------------------------------------------
def print_board(board):
    for row in board:
        print("|".join(row))
        print("-" * 5)

# --------------------------------------------------------------
# --------------------------------------------------------------
def check_winner(board):
    # Check rows, columns, and diagonals for a win
    for row in board:
        if row[0] == row[1] == row[2] and row[0] != ' ':
            return row[0]
    
    for col in range(3):
        if board[0][col] == board[1][col] == board[2][col] and board[0][col] != ' ':
            return board[0][col]
    
    if board[0][0] == board[1][1] == board[2][2] and board[0][0] != ' ':
        return board[0][0]
    
    if board[0][2] == board[1][1] == board[2][0] and board[0][2] != ' ':
        return board[0][2]
    
    return None

# --------------------------------------------------------------
# --------------------------------------------------------------
def is_board_full(board):
    return all(cell != ' ' for row in board for cell in row)

# --------------------------------------------------------------
# --------------------------------------------------------------
def minmax(board, is_maximizing):
    winner = check_winner(board)
    if winner == 'X':
        return 1
    elif winner == 'O':
        return -1
    elif is_board_full(board):
        return 0

    if is_maximizing:
        best_score = -float('inf')
        for i in range(3):
            for j in range(3):
                if board[i][j] == ' ':
                    board[i][j] = 'X'  # AI's move
                    score = minmax(board, False)
                    board[i][j] = ' '
                    best_score = max(score, best_score)
        return best_score
    else:
        best_score = float('inf')
        for i in range(3):
            for j in range(3):
                if board[i][j] == ' ':
                    board[i][j] = 'O'  # Opponent's move
                    score = minmax(board, True)
                    board[i][j] = ' '
                    best_score = min(score, best_score)
        return best_score

# --------------------------------------------------------------
# --------------------------------------------------------------

def ai_move(board):
    best_score = -float('inf')
    best_move = None

    for i in range(3):
        for j in range(3):
            if board[i][j] == ' ':
                board[i][j] = 'X'  # AI's move
                score = minmax(board, False)
                board[i][j] = ' '
                
                if score > best_score:
                    best_score = score
                    best_move = (i, j)
    
    if best_move:
        board[best_move[0]][best_move[1]] = 'X'

# --------------------------------------------------------------
# --------------------------------------------------------------

def player_move(board):
    while True:
        try:
            row = int(input("Enter row (1, 2, or 3): ")) - 1
            col = int(input("Enter column (1, 2, or 3): ")) - 1
            if board[row][col] == ' ':
                board[row][col] = 'O'
                break
            else:
                print("Spot already taken, try again!")
        except (ValueError, IndexError):
            print("Invalid input. Please enter numbers between 1 and 3.")

# --------------------------------------------------------------
# --------------------------------------------------------------

def play_game():
    print("Welcome to Tic-Tac-Toe!")
    print_board(board)
    
    while True:
        player_move(board)
        print_board(board)
        
        if check_winner(board):
            print("You win!")
            break
        if is_board_full(board):
            print("It's a draw!")
            break
        
        print("AI's turn...")
        ai_move(board)
        print_board(board)
        
        if check_winner(board):
            print("AI wins!")
            break
        if is_board_full(board):
            print("It's a draw!")
            break

# --------------------------------------------------------------
# --------------------------------------------------------------

if __name__ == "__main__":
    play_game()

# --------------------------------------------------------------
# --------------------------------------------------------------
