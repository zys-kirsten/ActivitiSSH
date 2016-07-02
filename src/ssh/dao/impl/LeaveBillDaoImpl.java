package ssh.dao.impl;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ssh.dao.ILeaveBillDao;
import ssh.domain.Employee;
import ssh.domain.LeaveBill;
import ssh.utils.SessionContext;

public class LeaveBillDaoImpl extends HibernateDaoSupport implements ILeaveBillDao {

	@Override
	public List<LeaveBill> findLeaveBillList() {
	
		String hql = "from LeaveBill o where o.user.name=?";
		String employee = SessionContext.get().getName();
		List<LeaveBill> list = this.getHibernateTemplate().find(hql, employee);
		if(list == null || list.size() == 0)
			return null;
		return list;
	}

	//保存请假单
	@Override
	public void saveLeaveBill(LeaveBill leaveBill) {
		this.getHibernateTemplate().save(leaveBill);
	}

	@Override
	public LeaveBill findLeaveBillById(Long id) {
		return this.getHibernateTemplate().get(LeaveBill.class, id);
	}

	@Override
	public void updateLeaveBill(LeaveBill leaveBill) {
		this.getHibernateTemplate().update(leaveBill);
		
	}

	@Override
	public void deleteLeaveBillById(Long id) {
		  LeaveBill leaveBill = this.findLeaveBillById(id);
		  this.getHibernateTemplate().delete(leaveBill);
	}

	
}
