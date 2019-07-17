package com.zhongyou.jobschedule;

public class JobSchedule implements Comparable<JobSchedule>, Runnable {
	private Runnable mTask;
	private long mScheduleAt;
	private boolean mIsScheduled;
	private boolean mIsExecuted;
	private boolean mIsCanceled;
	private CancelListener mCancelListener;


	public JobSchedule(Runnable task, long scheduleAt) {
		mTask = task;
		mScheduleAt = scheduleAt;
	}

	public JobSchedule(Runnable task) {
		mTask = task;
		mScheduleAt = 0L;
	}

	public long getSchedule() {
		return mScheduleAt;
	}

	void setScheduleAt(long scheduleAt) {
		mScheduleAt = scheduleAt;
	}

	public boolean isScheduled() {
		return mIsScheduled;
	}

	void setScheduled() {
		mIsScheduled = true;
	}

	public boolean isExecuted() {
		return mIsExecuted;
	}

	public void cancel() {
		CancelListener cancelListener = mCancelListener;
		if (cancelListener != null) {
			cancelListener.onCancel(this);
		}
		mIsCanceled = true;
	}

	public boolean isCanceled() {
		return mIsCanceled;
	}

	protected boolean isSupportReuse() {
		return false;
	}

	protected void executeTask(Runnable task) {
		task.run();
	}

	@Override
	public void run() {
		if (!isSupportReuse() && mIsExecuted) {
			throw new RuntimeException("Executed twice!");
		}
		if (mIsCanceled) {
			return;
		}
		mIsExecuted = true;
		executeTask(mTask);
	}

	@Override
	public int compareTo(JobSchedule o) {
		long diff = mScheduleAt - o.mScheduleAt;
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		} else {
			return 0;
		}
	}

	public void setCancelListener(CancelListener cancelListener) {
		mCancelListener = cancelListener;
	}

	public interface CancelListener {
		void onCancel(JobSchedule jobSchedule);
	}
}
