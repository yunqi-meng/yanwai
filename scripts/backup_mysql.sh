#!/bin/bash

# MySQL 数据库备份脚本
# 使用方法: ./backup_mysql.sh [备份目录]

# 配置
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_NAME="${DB_NAME:-yanwai}"
BACKUP_DIR="${1:-./backups}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"

# 日期格式
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${DATE}.sql"
BACKUP_FILE_GZ="${BACKUP_FILE}.gz"

# 创建备份目录
mkdir -p "${BACKUP_DIR}"

echo "开始备份数据库 ${DB_NAME}..."
echo "备份时间: $(date)"
echo "备份文件: ${BACKUP_FILE_GZ}"

# 执行备份
if [ -z "${DB_PASSWORD}" ]; then
    mysqldump -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --add-drop-database \
        --databases "${DB_NAME}" > "${BACKUP_FILE}"
else
    mysqldump -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --add-drop-database \
        --databases "${DB_NAME}" > "${BACKUP_FILE}"
fi

# 检查备份是否成功
if [ $? -eq 0 ]; then
    # 压缩备份文件
    gzip "${BACKUP_FILE}"
    
    if [ -f "${BACKUP_FILE_GZ}" ]; then
        BACKUP_SIZE=$(du -h "${BACKUP_FILE_GZ}" | cut -f1)
        echo "备份成功!"
        echo "备份大小: ${BACKUP_SIZE}"
        
        # 清理过期备份
        echo "清理 ${RETENTION_DAYS} 天前的备份..."
        find "${BACKUP_DIR}" -name "*.sql.gz" -type f -mtime +${RETENTION_DAYS} -delete
        
        REMAINING=$(find "${BACKUP_DIR}" -name "*.sql.gz" -type f | wc -l)
        echo "当前备份数量: ${REMAINING}"
    else
        echo "错误: 压缩备份文件失败"
        exit 1
    fi
else
    echo "错误: 数据库备份失败"
    rm -f "${BACKUP_FILE}"
    exit 1
fi

echo "备份完成: $(date)"
