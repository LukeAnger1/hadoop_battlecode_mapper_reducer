import argparse

def get_first_line(file_path):
    try:
        with open(file_path, 'r') as file:
            return file.readline().strip()
    except Exception as e:
        return f"An error occurred: {e}"

def main():
    parser = argparse.ArgumentParser(description='Read the first line of a file.')
    parser.add_argument('--path', type=str, default='matches', help='Path to the file')
    
    args = parser.parse_args()
    first_line = get_first_line(args.path)
    print(first_line)

if __name__ == "__main__":
    main()
