package ssh.service.impl;

import ssh.dao.IEmployeeDao;
import ssh.domain.Employee;
import ssh.service.IEmployeeService;

public class EmployeeServiceImpl implements IEmployeeService {

	private IEmployeeDao employeeDao;

	public void setEmployeeDao(IEmployeeDao employeeDao) {
		this.employeeDao = employeeDao;
	}

	//使用用户名作为条件查询用户对象
	@Override
	public Employee findEmployeeByName(String name) {
		Employee employee = employeeDao.findEmployeeByName(name);
		return employee;
	}
	
	
}
