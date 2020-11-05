package com.macfred.jobschedule;

import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class JobScheduler {

	public final void requestJobSchedule(JobSchedule schedule) {
		if (schedule instanceof TimerJobSchedule) {
			TimerJobSchedule timerJobSchedule = (TimerJobSchedule) schedule;
			timerJobSchedule.setListener(new TimerJobSchedule.Listener() {
				@Override
				public void onExecutionCountUpdate(int executionCount) {
					if (!timerJobSchedule.isCanceled() && !timerJobSchedule.isFinished()) {
						requestJobSchedule(timerJobSchedule);
					}
				}

				@Override
				public void onFinished() {

				}
			});
		}
		insertSchedule(schedule);
		schedule.setScheduled();
	}

	protected abstract void insertSchedule(JobSchedule schedule);

	public abstract Thread getExecutorThread();

	public boolean isInExecutorThread() {
		return Thread.currentThread() == getExecutorThread();
	}

	public <T> T asyncRunInSchedulerAndWait(Callable<T> callable, T defaultReturnValue) {
		if (isInExecutorThread()) {
			throw new RuntimeException("Only available in thread other than job scheduler thread");
		}
		Exchanger<Object> exchanger = new Exchanger<>();
		requestJobSchedule(new JobSchedule(() -> {
			try {
				exchanger.exchange(callable.call());
			} catch (Exception e) {
				try {
					exchanger.exchange(e, 50, TimeUnit.MILLISECONDS);
				} catch (InterruptedException | TimeoutException e1) {
					throw new RuntimeException(e);
				}
			}
		}));
		try {
			Object object = exchanger.exchange(null);
			if (object == null) {
				return defaultReturnValue;
			}
			if (object instanceof Throwable) {
				Throwable throwable = (Throwable) object;
				throw new RuntimeException(new RuntimeException(String.format("Exception encountered in job scheduler: %s", throwable.getMessage()), throwable));
			}
			return (T) object;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
