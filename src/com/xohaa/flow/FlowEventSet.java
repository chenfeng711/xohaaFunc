package com.xohaa.flow;
public interface FlowEventSet {
	/**
	 * 起草提交流程后
	 * @param doc
	 */
	public void doSubMitFlowStart()throws Exception;
	
	/**
	 * 提交到结束
	 * @param doc
	 */
	public void doSubMitFlowEnd()throws Exception;
	
	/**
	 * 退回到开始
	 * @param doc
	 * @throws NotesException 
	 */
	public void doReturnToStart() throws Exception;
	
	/**
	 * 终止流程
	 * @param doc
	 */
	public void doFlowStop() throws Exception;
	
	/**
	 * 流程事件处理
	 * @throws Exception
	 */
	public void doSubmitSet(int eventtype) throws Exception;
	
	/**
	 * 提交前检查
	 * @throws Exception
	 */
	public boolean doSubmitBeforeCheck() throws Exception;
}
