// MongoDB initialization script
db = db.getSiblingDB('guessgame');

// Create collections
db.createCollection('players');
db.createCollection('match_log');

// Create indexes for better performance
db.players.createIndex({ "nickname": 1 }, { unique: true, sparse: true });
db.players.createIndex({ "active": 1 });
db.players.createIndex({ "score": -1 });
db.players.createIndex({ "lastActivity": 1 });

db.match_log.createIndex({ "timestamp": -1 });
db.match_log.createIndex({ "event": 1 });
db.match_log.createIndex({ "connectionId": 1 });

// Insert sample data for testing (optional)
print('Database initialized successfully');
