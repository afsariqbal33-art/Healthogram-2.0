/**
 * Healthogram Unified Cloud Functions Architecture (Step 20)
 */
const auth = require('./auth');
const health = require('./health');
const social = require('./social');
const marketplace = require('./marketplace');
const payments = require('./payments');
const delivery = require('./delivery');
const messaging = require('./messaging');
const calling = require('./calling');
const translation = require('./translation');
const ai = require('./ai');
const notifications = require('./notifications');
const verification = require('./verification');
const admin = require('./admin');
const owner = require('./owner');
const analytics = require('./analytics');
const events = require('./events');
const tasks = require('./tasks');
const search = require('./search');
const shared = require('./shared');

module.exports = {
  auth,
  health,
  social,
  marketplace,
  payments,
  delivery,
  messaging,
  calling,
  translation,
  ai,
  notifications,
  verification,
  admin,
  owner,
  analytics,
  events,
  tasks,
  search,
  shared
};
