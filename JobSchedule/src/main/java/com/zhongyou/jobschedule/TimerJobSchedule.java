package com.zhongyou.jobschedule;

import com.zhongyou.util.utils.Wnn;

public class TimerJobSchedule extends JobSchedule {
	private final int mRepeat; // non-positive means infinite loop
	private final long mRepeatIntervalMilliseconds;
	private int mExecutionCount;
	private boolean mIsFinished;
	private Listener mListener;

	public TimerJobSchedule(Runnable task, long scheduleAt, int repeat, long repeatIntervalMilliseconds) {
		super(task, scheduleAt);
		mRepeat = repeat;
		mRepeatIntervalMilliseconds = repeatIntervalMilliseconds;
	}

	public TimerJobSchedule(Runnable task, int repeat, long repeatIntervalMilliseconds) {
		this(task, System.currentTimeMillis(), repeat, repeatIntervalMilliseconds);
	}

	@Override
	protected boolean isSupportReuse() {
		return true;
	}

	@Override
	protected void executeTask(Runnable task) {
		try {
			mExecutionCount++;
			task.run();
		} finally {
			if (mRepeat > 0 && mRepeat <= mExecutionCount) {
				mIsFinished = true;
				Wnn.c(mListener, Listener::onFinished);
			} else {
				long now = System.currentTimeMillis();
				long nextSchedule = Math.max(now, getSchedule() + mRepeatIntervalMilliseconds);
				setScheduleAt(nextSchedule);
				Wnn.c(mListener, listener -> listener.onExecutionCountUpdate(mExecutionCount));
			}
		}
	}

	public int getRepeat() {
		return mRepeat;
	}

	public long getRepeatIntervalMilliseconds() {
		return mRepeatIntervalMilliseconds;
	}

	public int getExecutionCount() {
		return mExecutionCount;
	}

	public boolean isFinished() {
		return mIsFinished;
	}

	public Listener getListener() {
		return mListener;
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	public interface Listener {
		void onExecutionCountUpdate(int executionCount);

		void onFinished();
	}
}
