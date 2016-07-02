package ssh.utils;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.struts2.ServletActionContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ssh.domain.Employee;
import ssh.service.IEmployeeService;

/**
 * 员工经理任务分配
 *
 */
//用类实现办理人必须实现TaskListener接口
@SuppressWarnings("serial")
public class ManagerTaskHandler implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
	
		//下面的做法会报错：懒加载异常（页面只有employee而没有他对应的manager）
//		//1.从session中获取当前用户
//		Employee employee = SessionContext.get();
//		 //2.设置个人任务的办理人
//		delegateTask.setAssignee(employee.getManager().getName());
		
		//正确做法：重新查询当前用户，再获取当前用户对应的领导
		Employee employee = SessionContext.get();
		String name = employee.getName();
		//从web中获取spring容器（该容器在web启动的时候就创建了，所以不用再次加载，只需直接获取）
		WebApplicationContext ac =WebApplicationContextUtils.getWebApplicationContext(ServletActionContext.getServletContext());
		IEmployeeService employeeService = (IEmployeeService) ac.getBean("employeeService");
		
		Employee emp = employeeService.findEmployeeByName(name);
		delegateTask.setAssignee(emp.getManager().getName());
	}

}
