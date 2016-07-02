package ssh.web.action;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import ssh.domain.LeaveBill;
import ssh.service.ILeaveBillService;
import ssh.service.IWorkflowService;
import ssh.utils.SessionContext;
import ssh.utils.ValueContext;
import ssh.web.form.WorkflowBean;

@SuppressWarnings("serial")
public class WorkflowAction extends ActionSupport implements ModelDriven<WorkflowBean> {

	private WorkflowBean workflowBean = new WorkflowBean();
	
	@Override
	public WorkflowBean getModel() {
		return workflowBean;
	}
	
	private IWorkflowService workflowService;
	
	private ILeaveBillService leaveBillService;

	public void setLeaveBillService(ILeaveBillService leaveBillService) {
		this.leaveBillService = leaveBillService;
	}

	public void setWorkflowService(IWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * 部署管理首页显示
	 * @return
	 */
	public String deployHome(){
		//查询部署对象信息
		List<Deployment> depList= workflowService.findDeployment();
		
		System.out.println(depList.size());
				
	    //查询流程定义信息
		List<ProcessDefinition> pdList = workflowService.findProcessDefinitionList();
		System.out.println(pdList.size());
		
		//放置上下文对象中
		ValueContext.putValueContext("depList", depList);
		ValueContext.putValueContext("pdList", pdList);
		return "deployHome";
	}
	
	/**
	 * 发布流程
	 * @return
	 */
	public String newdeploy(){
		//获取界面传递的值
		//1.获取页面上传递的zip格式的文件，格式是file类型
		File file = workflowBean.getFile();
		//文件名称
		String filename = workflowBean.getFilename();
		//完成部署
		workflowService.saveNewDeploye(file,filename);
		return "list";
	}
	
	/**
	 * 删除部署信息
	 */
	public String delDeployment(){
		//从页面获取部署对象id
		String deploymentId = workflowBean.getDeploymentId();
		//删除该部署
		workflowService.deleteProcessDefinitionByDeploymentId(deploymentId);
		return "list";
	}
	
	/**
	 * 查看流程图
	 * @throws Exception 
	 */
	public String viewImage() throws Exception{
		
		//1.获取页面参数：部署ID和资源文件名称
		String deploymentId = workflowBean.getDeploymentId();
		String imageName = workflowBean.getImageName();
		//2.获取资源文件的输入流
		InputStream in = workflowService.findImageInputStream(deploymentId,imageName);
		//3.获取response输出流
		OutputStream out = ServletActionContext.getResponse().getOutputStream();
		
		//4.向页面输出图片
		for(int b=-1;(b=in.read())!=-1;){
			out.write(b);
		}
		out.close();
		in.close();
		
 		return null;
	}
	
	// 启动流程
	public String startProcess(){
		//更新状态，启动流程实例，让启动的流程实例关联业务
		workflowService.saveStartProcess(workflowBean);
		return "listTask";
	}
	
	
	
	/**
	 * 任务管理首页显示
	 * @return
	 */
	public String listTask(){
		//从session中获取当前用户名
		String name = SessionContext.get().getName();
		List<Task> list = workflowService.findTaskListByName(name);
		ValueContext.putValueContext("list", list);
		return "task";
		
	}
	
	/**
	 * 打开任务表单
	 */
	public String viewTaskForm(){
		//获取任务id
		String taskId = workflowBean.getTaskId();
		String url = workflowService.findTaskFormKeyByTaskId(taskId);
		url += "?taskId="+taskId; 
		//获取任务表单中任务节点的url连接
		ValueContext.putValueContext("url", url);
		return "viewTaskForm";
	}
	
	// 准备表单数据
	public String audit(){
		//获取任务id
		String taskId = workflowBean.getTaskId();
		/**一.使用任务ID查找请假单ID，获取请假单信息*/
		LeaveBill leaveBill = workflowService.findLeaveBillByTaskId(taskId);
		ValueContext.putValueStack(leaveBill);
		
		/**二.已知任务ID，查询ProcessDefinitionEntity对象，从而获取当前任务完成后的连线名称，并放到list集合中*/
		List<String> outcomeList = workflowService.findOutComeListByTaskId(taskId);
		ValueContext.putValueContext("outcomeList", outcomeList);
		
		/**三.查询所有历史审核人的审核信息，帮助当前人完成审核返回List<Comment>*/
		List<Comment> commentList = workflowService.findCommentByTaskId(taskId);
		ValueContext.putValueContext("commentList", commentList);
		return "taskForm";
	}
	
	/**
	 * 提交任务
	 */
	public String submitTask(){
		workflowService.saveSubmitTask(workflowBean);
		
		return "listTask";
	}
	
	/**
	 * 查看当前流程图（查看当前活动节点，并使用红色的框标注）
	 */
	public String viewCurrentImage(){
		return "image";
	}
	
	// 查看历史的批注信息
	public String viewHisComment(){
		return "viewHisComment";
	}
}
