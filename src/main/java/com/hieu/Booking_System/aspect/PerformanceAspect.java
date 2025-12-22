package com.hieu.Booking_System.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    @Around("@annotation(com.hieu.Booking_System.aspect.LogTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis(); // 1. Bấm giờ bắt đầu

        // 2. Cho phép hàm thực sự chạy
        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis(); // 3. Bấm giờ kết thúc
        long duration = endTime - startTime; // 4. Tính thời gian

        // 5. In ra log
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Log màu vàng cho dễ nhìn: ClassName.MethodName took ... ms
        log.info("⏱️ EXECUTION TIME: {}.{}() took {} ms", className, methodName, duration);

        return result;
    }
}
