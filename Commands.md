## 命令

## 音符盒音高设置 (fixnotepitch)

### 语法
`/fixnotepitch <pos1> <pos2> [<pitch>]`
- 参数`<pitch>`是可选的，取值范围为0~24，默认值为0

### 效果
- 将`<pos1>`和`<pos2>`组成的立方体区域的音符盒音高设置为`<pitch>`

## 玩家动作 (playerOperate)

### 语法
- `/playerOperate ..`
    - `...<player>`
        - `...task`
            - `...vault`
            - `...pressUse`
        - `...stop`
        - `..pause`
        - `...resume`
    - `...list` 
    - `...pauseAll` 
    - `...resumeAll`

### 效果
- `/playerOperate ..`
    - `...<player>` 假玩家。
        - `...task`
            - `...vault [<maxCycles>]` 使假玩家执行开宝库的任务
                 - 使`<player>`长按右键100游戏刻后下线，并在游戏刻21刻后召唤一个`<player>_1`假人，视角和坐标不变，`<player>_1`假人继续长按右键100刻后下线，21刻后召唤`<player>_2`，一直循环到`<player>_[<maxCycles>]`，`[<maxCycles>]`默认为130。
            - `...pressUse <interval> <duration> [<cycles>]` 使假玩家间隔`<interval>`tick长按右键`<duration>`tick，重复`[<cycles>]`次，`[<cycles>]`默认为Infinite，当`[<cycles>]`为1时，`<interval>`值无用。
        - `...stop` 停止该玩家的任务。
        - `...pause` 暂停该玩家的任务
        - `...resume` 继续该玩家的任务（已暂停）
    - `...list` 查看所有任务列表。
    - `...pauseAll` 暂停所有任务。
    - `...resumeAll` 继续所有任务。

## 清空光照队列 (clearlightqueue)

### 语法
- `/clearlightqueue`

### 效果
- 直接清空待处理的光照队列。