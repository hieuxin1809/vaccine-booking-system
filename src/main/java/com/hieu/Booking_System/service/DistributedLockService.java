package com.hieu.Booking_System.service;

public interface DistributedLockService {
    boolean acquireLock(String lockName);
    void releaseLock(String lockName);
}
