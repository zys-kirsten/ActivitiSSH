package ssh.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;

import ssh.dao.ILeaveBillDao;
import ssh.domain.LeaveBill;
import ssh.service.IWorkflowService;
import ssh.utils.SessionContext;
import ssh.web.form.WorkflowBean;

public class WorkflowServiceImpl implements IWorkflowService {
	/**请假申请Dao*/
	private ILeaveBillDao leaveBillDao;
	
	private RepositoryService repositoryService;
	
	private RuntimeService runtimeService;
	
	private TaskService taskService;
	
	private FormService formService;
	
	private HistoryService historyService;
	
	public void setLeaveBillDao(ILeaveBillDao leaveBillDao) {
		this.leaveBillDao = leaveBillDao;
	}

	public void setHistoryService(HistoryService historyService) {
		this.historyService = historyService;
	}
	
	public void setFormService(FormService formService) {
		this.formService = formService;
	}
	
	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	
	//部署流程定义
	@Override
	public void saveNewDeploye(File file, String filename) {
		try {
			ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
			repositoryService.createDeployment()//创建部署对象
			                 .name(filename)//添加部署名称
			                 .addZipInputStream(zipInputStream)
			                 .deploy();//完成部署
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	//查询部署对象信息
	@Override
	public List<Deployment> findDeployment() {
  
		List<Deployment> list = repositoryService.createDeploymentQuery()//创建部署对象查询
		                       .orderByDeploymentName().asc()
		                       .list();
		return list;
	}

	//查询流程定义信息
	@Override
	public List<ProcessDefinition> findProcessDefinitionList() {
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()//常见流程定义查询对象
				                                        .orderByProcessDefinitionVersion().asc()
				                                        .list();
		return list;
	}

	//查看资源图片
	@Override
	public InputStream findImageInputStream(String deploymentId, String imageName) {
		return repositoryService.getResourceAsStream(deploymentId, imageName);
	}
	
	//删除流程部署
	@Override
	public void deleteProcessDefinitionByDeploymentId(String deploymentId) {

		repositoryService.deleteDeployment(deploymentId);
	}

	////更新状态，启动流程实例，让启动的流程实例关联业务
	@Override
	public void saveStartProcess(WorkflowBean workflowBean) {
		//1.获取请假单id，使用请假单查询请假单对象信息。更新状态
		Long id = workflowBean.getId();
		LeaveBill leaveBill = leaveBillDao.findLeaveBillById(id);
		leaveBill.setState(1);
		//2.使用当前对象获取流程定义的key（key与累名相同）
		String key = leaveBill.getClass().getSimpleName();
		//3.从session中获取当前任务的办理人，使用流程变量设置下一个任务的办理人。inputUser是流程变量的名称，获取的办理人是流程变量的值
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("inputUser",SessionContext.get().getName());
		
		//*4.1使用流程变量设置的字符串(格式：LeaveBill.id的形式)，通过设置，让启动的流程与业务关联
		String objId = key+"."+id;
		variables.put("objId", objId);
		//启动流程。同时设置流程变量；让流程关联业务
		 runtimeService.startProcessInstanceByKey(key,variables);
		
		
		//*4.2使用正在执行对象表中的字段business_key（Activiti提供的字段）,让启动的流程（流程实例）关联业务
		//String objId = key+"."+id;
		//启动流程。同时设置流程变量；同时向正在执行的执行对象表中的字段business_key添加业务数据，同时让流程关联业务
		//runtimeService.startProcessInstanceByKey(key,objId,variables);
		 //4.1与4.2可以同时使用
		
	}

	//查询正在执行的任务
	@Override
	public List<Task> findTaskListByName(String name) {
  
		List<Task> list = taskService.createTaskQuery()
				                     .taskAssignee(name)
				                     .orderByTaskCreateTime().asc()
				                     .list();
		return list;
	}

	//使用任务ID，获取当前任务节点中对应的FormKey中连接的值
	@Override
	public String findTaskFormKeyByTaskId(String taskId) {
       TaskFormData taskFormData = formService.getTaskFormData(taskId);
		return taskFormData.getFormKey();
	}
	
	//通过taskId获取请假单信息
	@Override
	public LeaveBill findLeaveBillByTaskId(String taskId) {

		//1.使用任务ID，查询任务对象Task
		Task task = taskService.createTaskQuery()
		           .taskId(taskId)
		           .singleResult();
		//2.使用任务对象Task获取流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//3.使用流程实例ID查询正在执行的执行对象表，返回流程实例对象
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
				                           .processInstanceId(processInstanceId)
				                           .singleResult();
		//4.使用流程实例对象获取BUSINESS_KEY
		String businessKey = pi.getBusinessKey();
		//5.获取BUSINESS_KEY对应的主键ID，使用主键ID，查询请假单对象
		LeaveBill leaveBill= null;
		String id = "";
		if (StringUtils.isNotBlank(businessKey)) {
			//截取字符串，取businesskey小数点的第二个值
			id = businessKey.split("\\.")[1];
			leaveBill = leaveBillDao.findLeaveBillById(Long.parseLong(id));
		}
		return leaveBill;
	}
	
	////已知任务ID，查询ProcessDefinitionEntity对象，从而获取当前任务完成后的连线名称，并放到list集合中
	@Override
	public List<String> findOutComeListByTaskId(String taskId) {
		List<String> liststring = new ArrayList<>();
		//1.使用任务ID查询任务对象
		Task task = taskService.createTaskQuery()
		           .taskId(taskId)
		           .singleResult();
		//2.获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//3.查询ProcessDefinitionEntity对象
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
		
		//使用任务对象Task获取流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//使用流程实例ID查询正在执行的执行对象表，返回流程实例对象
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
						                           .processInstanceId(processInstanceId)
						                           .singleResult();
		//获取当前活动的id
		String activityId = pi.getActivityId();
		//4.获取当前活动
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
		//5.获取当前活动完成之后的连线名称
		List<PvmTransition> list = activityImpl.getOutgoingTransitions();
		if (list != null && list.size()>0) {
			for(PvmTransition pvmTransition:list){
				String name = (String) pvmTransition.getProperty("name");
				if (StringUtils.isNotBlank(name)) {
					liststring.add(name);
				}else{
					liststring.add("默认提交");
				}
			}
		}
		return liststring;
	}
	
	//指定连线名称完成任务
	@Override
	public void saveSubmitTask(WorkflowBean workflowBean) {
	
		//获取任务ID
		String taskId = workflowBean.getTaskId();
		
		//获取连线名称
		String outcome = workflowBean.getOutcome();
		
		//获取批注信息
		String message = workflowBean.getComment();
		
		//获取请假单id
		Long id = workflowBean.getId();
		
		//1.在完成之前，添加一个批注信息，用于记录当前申请人的一些审核信息
		//使用任务ID，查询任务对象Task
				Task task = taskService.createTaskQuery()
				           .taskId(taskId)
				           .singleResult();
	   //使用任务对象Task获取流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		/**
		 * 注意：添加批注的时候，由于Activiti底层代码，有setUserId（userId）
		 * 所以需要从session中获取当前登录人作为任务的办理人（审核人），对应act_hi_comment表中的User_ID字段，不添加的话，该字段为null
		 */
		Authentication.setAuthenticatedUserId(SessionContext.get().getName());
		taskService.addComment(taskId, processInstanceId, message);
		
		/**2.如果连线的名称是“默认提交”，那就不需要设置流程变量，否则需要设置流程变量，按照连线的名称，去完成任务*/
		Map<String, Object> variables = new HashMap<>();
		if (outcome != null &&  !outcome.equals("默认提交")) {
			variables.put("outcome", outcome);
		}
		
		/**3.使用任务ID,完成当前人的个人任务*/
		taskService.complete(taskId, variables);
		
		//4.任务完成后需要指定下一个任务的办理人（使用类ManagerTaskHandler）已完成
		
		//5.完成任务之后，判断流程是否结束，如果流程结束，更新请假单表的状态从1变成2（审核中变成已完成）
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
		              .processInstanceId(processInstanceId)//流程实例id查询
		              .singleResult();
		
		if (pi == null) {
			LeaveBill leaveBill = leaveBillDao.findLeaveBillById(id);
			leaveBill.setState(2);
		}
	}
	
	
	//获取批注信息,传递的是当前的任务id，要获取历史任务id对应的批注
	@Override
	public List<Comment> findCommentByTaskId(String taskId) {
		List<Comment> list = new ArrayList<>();
		//使用当前的任务id，查询当前流程对应的历史任务id
		
		//使用任务ID，查询任务对象Task
		Task task = taskService.createTaskQuery()
		           .taskId(taskId)
		           .singleResult();
		//使用任务对象Task获取流程实例ID
				String processInstanceId = task.getProcessInstanceId();
		//使用流程实例id，查询历史任务，获取历史任务对应的每个任务id
	List<HistoricTaskInstance> historicTaskInstances =	historyService.createHistoricTaskInstanceQuery()
		              .processInstanceId(processInstanceId)
		              .list();
	
	//遍历集合，获取每个任务id
	if (historicTaskInstances != null && historicTaskInstances.size()>0) {
		for(HistoricTaskInstance hti:historicTaskInstances){
			//任务ID
			String htaskId = hti.getId();
			//获取批注信息
			List<Comment> taskList = taskService.getTaskComments(htaskId);
			list.addAll(taskList);
		}
	}
		
		return list;
	}
}
