#!/bin/sh
touch /home/ba/log.text
echo "自动上传笔记" >> /home/ba/log.text
cd /home/ba/AndroidStudioProjects/BANote
git add .
git commit -m "笔记"
git pull
git push
echo "自动上传笔记成功" >> /home/ba/log.text
exit 0;
