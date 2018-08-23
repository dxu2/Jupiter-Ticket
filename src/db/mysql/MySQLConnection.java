package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {

	private Connection conn;

	public MySQLConnection() {
		// TODO Auto-generated constructor stub
		try {
			Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}

		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}

		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return new HashSet<>();
		}

		Set<String> favoriteItems = new HashSet<>();

		try {
			String sql = "select item_id from history where user_id = ?";
			PreparedStatement preparedStatement;
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, userId);
			ResultSet rSet = preparedStatement.executeQuery();
			while (rSet.next()) {
				String itemId = rSet.getString("item_id");
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return new HashSet<>();
		}

		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);

		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, itemId);

				ResultSet rs = stmt.executeQuery();

				ItemBuilder builder = new ItemBuilder();

				while (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));

					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return null;
		}
		Set<String> categories = new HashSet<>();
		try {
			String sql = "Select category from categories where item_id = ?";
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, itemId);
			ResultSet rSet = preparedStatement.executeQuery();
			while (rSet.next()) {
				String category = rSet.getString("category");
				categories.add(category);
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// TODO Auto-generated method stub
		TicketMasterAPI ticketMasterAPI = new TicketMasterAPI();
		List<Item> items = ticketMasterAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		try {
			String sql = "Insert ignore into items values(?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement pStatement = conn.prepareStatement(sql);
			pStatement.setString(1, item.getItemId());
			pStatement.setString(2, item.getName());
			pStatement.setDouble(3, item.getRating());
			pStatement.setString(4, item.getAddress());
			pStatement.setString(5, item.getImageUrl());
			pStatement.setString(6, item.getUrl());
			pStatement.setDouble(7, item.getDistance());
			pStatement.execute();

			sql = "insert ignore into categories values(?, ?)";
			pStatement = conn.prepareStatement(sql);
			pStatement.setString(1, item.getItemId());
			for (String category : item.getCategories()) {
				pStatement.setString(2, category);
				pStatement.execute();
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return "";
		}
		String name = "";
		try {
			String sql = "Select first_name, last_name from users where user_id = ?";
			PreparedStatement pStatement = conn.prepareStatement(sql);
			pStatement.setString(1, userId);
			ResultSet rSet = pStatement.executeQuery();

			while (rSet.next()) {
				name = String.join("", rSet.getString("first_name"), rSet.getString("last_name"));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return false;
		}

		try {
			String sql = "select user_id from users where user_id = ? and password = ?";
			PreparedStatement pStatement = conn.prepareStatement(sql);
			pStatement.setString(1, userId);
			pStatement.setString(2, password);
			ResultSet resultSet = pStatement.executeQuery();
			while (resultSet.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return false;
	}

}
