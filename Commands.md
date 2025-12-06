## 命令

## 音符盒音高设置 (fixnotepitch)

### 语法
`/fixnotepitch <pos1> <pos2> [<pitch>]`
- 参数`<pitch>`是可选的，取值范围为0~24，默认值为0

### 效果
- 将`<pos1>`和`<pos2>`组成的立方体区域的音符盒音高设置为`<pitch>`

## 玩家动作 (playerOperate)

### 语法
- `/player Operate <player>/list ..` 
    - `...vault`
    - `...stop`

### 效果
- `/player Operate <player>/list ..`
    - `...vault [<maxCycles>]` 使假玩家执行开宝库的任务
        - 使`<player>`长按右键100游戏刻后下线，并在游戏刻21刻后召唤一个`<player>_1`假人，视角和坐标不变，`<player>_1`假人继续长按右键100刻后下线，21刻后召唤`<player>_2`，一直循环到`<player>_[<maxCycles>]`，`[<maxCycles>]`默认为130
    - `...stop`
        - 停止该玩家的所有任务 

## 清空光照队列 (clearlightqueue)

### 语法
- `/clearlightqueue`

### 效果
- 直接清空待处理的光照队列。