import sys
from fuzzywuzzy import fuzz
from fuzzywuzzy import process

def main(args):
 if len(args) > 2:
  print fuzz.ratio(args[1], args[2])

if __name__ == '__main__':
 main(sys.argv)