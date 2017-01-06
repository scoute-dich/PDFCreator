package com.artifex.mupdfdemo;

public interface CancellableTaskDefinition <Params, Result>
{
	Result doInBackground(Params... params);
	void doCancel();
	void doCleanup();
}
