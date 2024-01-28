import site, math
def greet():
    print('Hello World!')

# function with two arguments
def add_numbers(num1, num2):
    sum = num1 + num2
    print('Sum: ',sum)
    return str(sum)

# function definition
def find_square(num):
    result = num * num
    return str(result)
    
def get_square(num):
    return str(num * num)
    
def get_root(num):
    square_root = math.sqrt(num)
    return str(square_root)

    