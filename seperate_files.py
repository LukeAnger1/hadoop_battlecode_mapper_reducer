def split_file(input_file, output_directory, lines_per_file):
    with open(input_file, 'r') as infile:
        data = infile.read().splitlines()

    # Calculate the number of files needed
    num_files = len(data) // lines_per_file + 1

    for i in range(num_files):
        # Generate a new file name
        output_file = f"{output_directory}/matches_{i + 1}.txt"

        # Determine the start and end indices for this chunk
        start_idx = i * lines_per_file
        end_idx = (i + 1) * lines_per_file

        # Write the chunk to a new file
        with open(output_file, 'w') as outfile:
            outfile.write('\n'.join(data[start_idx:end_idx]))

if __name__ == "__main__":
    input_file = "/home/hduser/battlecode_beasts/matches"
    output_directory = "/home/hduser/battlecode_beasts/match_holder"
    lines_per_file = 10  # Adjust this value based on your requirements

    # Create the output directory if it doesn't exist
    import os
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)

    split_file(input_file, output_directory, lines_per_file)
