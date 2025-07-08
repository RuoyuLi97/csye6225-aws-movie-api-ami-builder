import sys

if len(sys.argv) != 2:
  print("Usage: python result_checker.py <target assignment>")
  exit(1)

target_assignment = sys.argv[1]
with open("grader_result.txt", "r") as f:
  # Read all lines in one string
  lines = f.read()

  # Check if the target assignment is in the result
  if not f'{target_assignment} test passed.' in lines:
    exit(1)

print(f'{target_assignment} test passed.')