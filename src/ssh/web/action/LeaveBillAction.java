package ssh.web.action;

import ssh.domain.Employee;
import ssh.domain.LeaveBill;
import ssh.service.ILeaveBillService;
import ssh.utils.SessionContext;
import ssh.utils.ValueContext;

import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@SuppressWarnings("serial")
public class LeaveBillAction extends ActionSupport implements ModelDriven<LeaveBill> {

	private LeaveBill leaveBill = new LeaveBill();
	
	@Override
	public LeaveBill getModel() {
		return leaveBill;
	}
	
	private ILeaveBillService leaveBillService;

	public void setLeaveBillService(ILeaveBillService leaveBillService) {
		this.leaveBillService = leaveBillService;
	}

	/**
	 * 请假管理首页显示
	 * @return
	 */
	public String home(){
		
		//查询自己的请假单
		List<LeaveBill> list = leaveBillService.findLeaveBillList();
		ValueContext.putValueContext("list", list);
		return "home";
	}
	
	/**
	 * 添加请假申请
	 * @return 
	 */
	public String input(){
		//获取请假单id
		Long id = leaveBill.getId();
		
		//修改
		if(id!=null){
			//查询请假单信息
			LeaveBill bill = leaveBillService.findLeaveBillById(id);
			//将信息放到栈顶，页面使用struts2标签，支持表单回显
			ValueContext.putValueStack(bill);
		}
		//新增
		return "input";
	}
	
	/**
	 * 保存/更新，请假申请
	 * 
	 * */
	public String save() {
		//执行保存
		leaveBillService.saveLeaveBill(leaveBill);
		return "save";
	}
	
	/**
	 * 删除，请假申请
	 * 
	 * */
	public String delete(){
		//先获取请假单id
		Long id = leaveBill.getId();
		//执行删除
		leaveBillService.deleteLeaveBillById(id);
		return "save";
	}
	
}
