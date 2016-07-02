package ssh.utils;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

import ssh.domain.Employee;


/**
 * 登录验证拦截器
 *
 */
@SuppressWarnings("serial")
public class LoginInteceptor implements Interceptor {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	//每次访问action之前先执行intercept方法
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		
		//获取当前访问的URL
		String acrionName = invocation.getProxy().getActionName();
		//如果访问的action是login_action,表示此事还没有session，需要放行
		
		if(!"loginAction_login".equals(acrionName)){
			//从session中获取当前用户对象
			Employee employee = SessionContext.get();
			
			//如果session不存在，跳转到登录界面
			if(employee == null){
				return "login";
			}
		}
		
		//放行，访问action类中的方法
		return invocation.invoke();
		
	}

}
