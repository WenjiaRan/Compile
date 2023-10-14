import os

# 设定当前工作目录
current_directory = os.getcwd()

# 获取当前目录下所有的文件名
all_files = os.listdir(current_directory)

# 创建新的txt文件用于写入内容
with open('output.txt', 'w', encoding='utf-8') as output_file:
    for filename in all_files:
        # 跳过新建的output.txt文件和目录
        if filename != 'output.txt' and os.path.isfile(filename) and filename != 'main.py':
            # 写入文件名
            output_file.write(f'===== {filename} =====\n')
            
            # 读取文件内容并写入，跳过空白行
            try:
                with open(filename, 'r', encoding='utf-8') as f:
                    for line in f:
                        # 判断是否为空白行
                        if line.strip():
                            output_file.write(line)
                    output_file.write('\n')
            except:
                output_file.write("Failed to read the content of this file due to encoding issues or other reasons.\n\n")

print("All filenames and their contents have been written to output.txt.")
