
import subprocess
import sys

# MySQL 连接信息
mysql_user = 'root'
mysql_password = 'lyp1234zhen'
database = 'yanwai'
sql_file = r'c:\Users\MECHREVO\Desktop\言外\AI_Chatbot_Project_Plan\add_new_data.sql'

# 使用 mysql 命令行工具执行 SQL 文件
try:
    cmd = [
        'mysql',
        f'--user={mysql_user}',
        f'--password={mysql_password}',
        database
    ]
    
    with open(sql_file, 'r', encoding='utf-8') as f:
        sql_content = f.read()
    
    # 执行 SQL
    result = subprocess.run(
        cmd,
        input=sql_content.encode('utf-8'),
        capture_output=True,
        text=False
    )
    
    if result.returncode == 0:
        print("新数据导入成功！")
    else:
        print(f"导入失败，错误信息: {result.stderr.decode('utf-8', errors='ignore')}")
        sys.exit(1)
        
except Exception as e:
    print(f"发生错误: {e}")
    sys.exit(1)

