package ssh.dao.impl;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ssh.dao.IEmployeeDao;
import ssh.domain.Employee;

public class EmployeeDaoImpl extends HibernateDaoSupport implements IEmployeeDao {

	@Override
	public Employee findEmployeeByName(String name) {
  
		String hql ="from Employee o where o.name=?";
		List<Employee> employees = this.getHibernateTemplate().find(hql, name);
		if(employees==null || employees.size() == 0)
			return null;
		return employees.get(0);
	}
	
	
}
