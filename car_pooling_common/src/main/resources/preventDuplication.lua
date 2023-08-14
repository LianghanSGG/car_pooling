--
-- Created by IntelliJ IDEA.
-- User: admin
-- Date: 2023/8/7
-- Time: 15:29
-- To change this template use File | Settings | File Templates.
--
-- 这里实现的是防止重复提交订单，利用lua脚本原子性
-- 传入的参数 KEY 前缀加用户id  ARGV accessToken
-- 返回的参参数 0 不存在 1 成功 2 校验失败 3删除失败

if redis.call('EXISTS', KEYS[1]) == 0 then
    return 0
end


if redis.call('GET', KEYS[1]) == ARGV[1] then
    if tonumber(redis.call('DEL', KEYS[1])) == 1 then
        return 1
    else
        return 3
    end
else
    return 2
end