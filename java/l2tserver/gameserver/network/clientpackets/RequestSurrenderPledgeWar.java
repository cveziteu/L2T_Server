/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2tserver.gameserver.network.clientpackets;

import l2tserver.gameserver.datatables.ClanTable;
import l2tserver.gameserver.model.L2Clan;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.entity.ClanWarManager;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.log.Log;

public final class RequestSurrenderPledgeWar extends L2GameClientPacket
{
	private static final String _C__51_REQUESTSURRENDERPLEDGEWAR = "[C] 51 RequestSurrenderPledgeWar";
	
	private String _pledgeName;
	private L2Clan _clan;
	private L2PcInstance _activeChar;
	
	@Override
	protected void readImpl()
	{
		_pledgeName  = readS();
	}
	
	@Override
	protected void runImpl()
	{
		_activeChar = getClient().getActiveChar();
		if (_activeChar == null)
			return;
		_clan = _activeChar.getClan();
		if (_clan == null)
			return;
		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		
		if (clan == null)
		{
			_activeChar.sendMessage("No such clan.");
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		Log.info("RequestSurrenderPledgeWar by "+getClient().getActiveChar().getClan().getName()+" with "+_pledgeName);
		
		if (!_clan.isAtWarWith(clan.getClanId()))
		{
			_activeChar.sendMessage("You aren't at war with this clan.");
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
		msg.addString(_pledgeName);
		_activeChar.sendPacket(msg);
		msg = null;
		_activeChar.deathPenalty(false, false, false, false);
		ClanWarManager.getInstance().getWar(_clan, clan).stop();
	}
	
	@Override
	public String getType()
	{
		return _C__51_REQUESTSURRENDERPLEDGEWAR;
	}
}