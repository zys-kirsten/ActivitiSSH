package ssh.service.impl;

import java.util.List;

import ssh.dao.ILeaveBillDao;
import ssh.domain.LeaveBill;
import ssh.service.ILeaveBillService;
import ssh.utils.SessionContext;

public class LeaveBillServiceImpl implements ILeaveBillService {

	private ILeaveBillDao leaveBillDao;

	public void setLeaveBillDao(ILeaveBillDao leaveBillDao) {
		this.leaveBillDao = leaveBillDao;
	}

	//查询自己的请假单
	@Override
	public List<LeaveBill> findLeaveBillList() {
		return leaveBillDao.findLeaveBillList();
	}

	//保存请假单
	@Override
	public void saveLeaveBill(LeaveBill leaveBill) {

		//获取请假单id
		Long id = leaveBill.getId();
		
		//进行保存操作
		if (id == null) {
			//1.从session中获取当前用户对象，将LeaveBill对象中的user与session中获取的用户对象进行关联
			leaveBill.setUser(SessionContext.get());//建立管理关系
			//2.保存
			leaveBillDao.saveLeaveBill(leaveBill);
		}else {
			//进行更新操作
			leaveBillDao.updateLeaveBill(leaveBill);
		}
		
	}

	@Override
	public LeaveBill findLeaveBillById(Long id) {
		return leaveBillDao.findLeaveBillById(id);
	}

	@Override
	public void deleteLeaveBillById(Long id) {
		leaveBillDao.deleteLeaveBillById(id);
		
	}

}
