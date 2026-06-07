import csv

# Put your raw CSV filename here
input_file = "1st3rd.csv"

with open(input_file, mode="r", encoding="utf-8") as file:
    reader = csv.reader(file)
    for index, row in enumerate(reader, start=1):
        # 1. Strip the prefixes (e.g., '3H' -> 'H')
        clean_letters = [item.strip()[1:] for item in row if len(item.strip()) >= 2]

        # 2. Sort them strictly from A to Z
        sorted_letters = sorted(clean_letters)

        # 3. Join them into a single string
        result_string = "".join(clean_letters)
        result_sorted_string = "".join(sorted_letters)

        if index == 1:
            result1_string = result_string
        else:
            paired_list = []
            for base_char, current_char in zip(result1_string, result_string):
                paired_list.append(f"{base_char}{current_char}")
                # Join the pairs with a comma
                result2_string = ",".join(paired_list)

            #print(f"{result_sorted_string} {result2_string}")
            print(f"({53+index-1}, \'WC2026_1ST3RD\', \'{result_sorted_string}\', \'{result2_string}\'),")
