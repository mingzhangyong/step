写了一段时间的后台，记录一下mysql相关的操作。

### 关联查询和连接查询
```mysql
    SELECT * FROM student a, score b WHERE a.st_id = b.st_id; 
```
```mysql
    SELECT * FROM student a LEFT JOIN score b ON a.st_id = b.st_id; 
```
连接查询会查询出a表所有的行，即使b表中没有匹配的行。    同理  right join是查询出 右边的表所有行。

### 通配符
通配符 | 描述
------ | ----
%      | 代替一个或多个字符
_      | 代替一个字符
[a ,b ,c , ... ] | 与列表中任意一个字符匹配即可
[^a,b,c,...] 或者 [!a,b,c,...]      | 不存在字符列表中任何一个字符匹配

<div style="color:red" > mysql不支持 [a ,b ,c , ... ] 这种形式的通配符，而是用正则表达式来匹配，并且不是用LIKE匹配而是用RLIKE</div>

示例：
```mysql
    SELECT * FROM student WHERE st_name RLIKE '[v+]'
```

### in和Between
```mysql
SELECT * FROM student a WHERE a.st_id in (SELECT st_id FROM score WHERE score>=90)
```

当然还有not in    

同理Between可以查两者之间的， 常用的查询时间段内的数据。
```mysql
SELECT * FROM student a WHERE a.st_id in (SELECT st_id FROM score WHERE score BETWEEN 40 AND 60 )
```


concat

case when
