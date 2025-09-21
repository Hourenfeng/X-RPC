package org.xhy.tolerant;

import com.github.rholder.retry.*;
import org.xhy.common.URL;
import org.xhy.common.constants.LoadBalance;
import org.xhy.router.LoadBalancer;
import org.xhy.router.LoadBalancerFactory;
import org.xhy.utils.CheckedFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 针对“一个 URL 内部多次重试 + 失败后剔除节点并重新负载均衡”场景的统一策略。
 */
public class FailoverRetryPolicy {

    /**
     * 每个 URL 最多重试几次
     */
    private final int perUrlAttempts;
    /**
     * URL 内部重试的等待策略
     */
    private final WaitStrategy waitStrategy;

    public FailoverRetryPolicy(int perUrlAttempts, long waitMillis) {
        this.perUrlAttempts = perUrlAttempts;
        this.waitStrategy = WaitStrategies.fixedWait(waitMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * @param urls   初始可用 URL 列表
     * @param action 发送 RPC 的真正逻辑：给定 URL → 返回结果
     */
    public <T> T execute(List<URL> urls, CheckedFunction<URL, T> action) throws Exception {
        // 拷贝，避免修改原 List
        List<URL> candidates = new ArrayList<>(urls);

        // 外层 while：失效节点剔除 + 重新负载均衡
        while (!candidates.isEmpty()) {

            // 选节点 —— 沿用你已有的负载均衡工厂
//            LoadBalancer lb = LoadBalancerFactory.get(LoadBalance.Random);
//            URL url = lb.select(candidates);
            URL url = candidates.get(0);
            // 针对选中的 URL 构造一次“Guava Retryer”

            Retryer<T> retryer = RetryerBuilder.<T>newBuilder()
                    .retryIfException()                              // 捕获任何异常就重试
                    .withWaitStrategy(waitStrategy)                  // 固定等待
                    .withStopStrategy(StopStrategies
                            .stopAfterAttempt(perUrlAttempts))       // URL 内部最大 N 次
                    .withRetryListener(new RetryListener() {
                        @Override
                        public <V> void onRetry(Attempt<V> attempt) {
                            System.out.printf(
                                    "[重试次数: %d] URL: %s，异常信息: %s%n",
                                    attempt.getAttemptNumber(),
                                    url,
                                    attempt.hasException() ? attempt.getExceptionCause().toString() : "无"
                            );
                        }
                    })
                    .build();

            try {
                //  真正发请求；成功则直接返回
                return retryer.call(() -> action.apply(url));
            } catch (RetryException | ExecutionException e) {
                //  URL 在 N 次内仍失败 —— 剔除并进入下一轮
                    System.err.printf("URL %s 重试 %d 次后仍失败，剔除并重选%n", url, perUrlAttempts);
                candidates.remove(url);
            }
        }
        throw new Exception("所有节点都不可用，RPC 调用失败");
    }
}
