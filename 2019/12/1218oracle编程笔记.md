### Oracle数据库编程

使用场景：采集人员每天的轨迹里程

**需求** ：要求根据人员轨迹表数据，采集每个人每天的轨迹里程；

**分析**
- 人员轨迹表关键结构
    - 人员标识 | 上报时间 | 坐标x | 坐标
      -------- | -----  |     -------- | ----- 
- 要求每天采集，则今天采集昨天的， 考虑使用定时任务处理  Oracle dbms_job处理

- 考虑到其他地方可能会要求查询人员某个时间段内的里程，则将计算里程抽成一个方法。

- 计算两点之间距离已有相关的方法

**实现**

1.人员里程方法
```oracle
create or replace function fcGetPatrolDistance(
   mPatrolId integer,
   startTime date,
   endTime date) RETURN NUMBER  is
  i integer;
  j integer;
  posCount integer;
  currentX number;
  currentY number;
  iPatrolId number;
  iStartTime date;
  iEndTime date;
  nextX number;
  nextY number;
  coordType varchar2(100);
  coordinateType integer;
  sumDistance number;
  distance number;

  DEBUG_BEGIN_TIME number;

  type pos_array is table of dlmis.trlogpatrolpos % rowtype index by binary_integer;
  posTable pos_array;

begin
    DEBUG_BEGIN_TIME := dbms_utility.get_time;
    sumDistance := 0;
    iPatrolId := mPatrolId;
    iStartTime := startTime;
    iEndTime := endTime;
    coordinateType := 1;
     plog.info('计算轨迹里程 start');
     select * bulk collect into posTable from dlmis.trlogpatrolpos where PATROLID = iPatrolId and UPDATETIME BETWEEN iStartTime and iEndTime and coordinatex <> -1 order by UPDATETIME;
     posCount := posTable.count;
     i := 1;
     if posCount >= 1 then
     coordType := posTable(1).COORDTYPE;
     if(coordType = 'GPS') then coordinateType := 2;
     end if;
     if(coordType = '1') then coordinateType := 1;
     end if;
     while i < posCount loop
        currentX := posTable(i).COORDINATEX;
        currentY := posTable(i).COORDINATEY;
        nextX := posTable(i+1).COORDINATEX;
        nextY := posTable(i+1).COORDINATEY;
        distance := DLSYS.FCGEOGETDISTANCE(coordinateType,currentX,currentY,nextX,nextY);
        sumDistance := sumDistance + distance;
        i := i+1;
    end loop;
    end if;
    return sumDistance;
    end fcGetPatrolDistance;
```
- 方法介绍
    - 参数 ： 人员id ， 查询起始时间， 查询结束时间
    - 过程 ： 查出人员所有轨迹，遍历所有轨迹。计算当前坐标与下一个坐标距离，相加得到结果。
    
2.存储过程实现所有人员轨迹的统计（代码还包含统计人员在线时长）
```oracle
create or replace procedure umwarn.poStatPatrolDistanceAndTime  is
/**
    采集员前一天的里程和在线时长

    该存过一天执行一次。 当天查前一天的。
**/
  DEBUG_BEGIN_TIME number;
  type result_array is table of UMWARN.TCPATROLSTATINFO % rowtype index by binary_integer;
  resultTable result_array;
begin
    DEBUG_BEGIN_TIME := dbms_utility.get_time;
    delete from umwarn.tcpatrolstatinfo t where t.statdate > trunc(sysdate) - 1;
    insert into umwarn.tcpatrolstatinfo(patrolID,patrolName,cardid,onlineTime,distance,statDate)
    SELECT
    	a.PATROLID,
    	a.PATROLNAME,
    	a.cardid,
    	b.onlinTime,
    	umwarn.fcgetpatroldistance(
    		a.PATROLID,
    		TRUNC( SYSDATE )- 1,
    		TRUNC( SYSDATE )
    	) AS distance,
    	TRUNC( SYSDATE )- 1
    FROM
    	dlsys.tcPatrol a
    LEFT JOIN(
    		SELECT
    			STATDATE,
    			PATROLID,
    			SUM(( t.LOGOUTTIME - t.LOGINTIME )* 24 * 60) AS onlinTime
    		FROM
    			UMWARN.TCPATROLLOGONINFO t
    		WHERE
    			t.STATDATE = TRUNC( SYSDATE )- 1
    		GROUP BY
    			PATROLID,
    			STATDATE,
    			PATROLNAME
    	) b ON
    	a.PATROLID = b.PATROLID
    WHERE
    	PATROLTYPEID = 1;
    commit;
exception
  when others then rollback;
end poStatPatrolDistanceAndTime;
```

3.创建定时任务，定时执行
```oracle
DECLARE iJobID INTEGER;
BEGIN
    DBMS_JOB.SUBMIT(
     job       => iJobID,
     what      => 'umwarn.poStatPatrolDistanceAndTime();',
     next_date => trunc(sysdate),
     interval  =>  'TRUNC(SYSDATE + 1) + 1/24',
     no_parse  => TRUE
    );
COMMIT;
END;
```

- 定义在每天的凌晨1点执行该定时任务