const express = require('express');
const app = express();
const port = process.env.PORT || 3000;

// JSON ডেটা রিসিভ করার জন্য
app.use(express.json());

// বেসিক রাউট সার্ভার চেক করার জন্য
app.get('/', (req, res) => {
  res.send('Recharge API is running perfectly!');
});

// মোবাইল রিচার্জ করার API এন্ডপয়েন্ট
app.post('/api/recharge', (req, res) => {
  const { phone, amount, operator } = req.body;
  const apiKey = req.headers['x-api-key'];

  // API Key চেক করা হচ্ছে (নিরাপত্তার জন্য)
  // রেন্ডার (Render) এর এনভায়রনমেন্ট ভেরিয়েবল (Environment Variables) থেকে এটি আসবে
  const expectedApiKey = process.env.RECHARGE_API_KEY || 'my-recharge-api-key-placeholder';
  if (apiKey !== expectedApiKey) {
     return res.status(401).json({ error: 'Unauthorized! API Key is wrong.' });
  }

  // এখানে আপনি আপনার আসল থার্ড-পার্টি রিচার্জ API (যেমন BDTopup বা অন্য কোনো) কানেক্ট করবেন
  console.log(`Recharge Request Received: Phone: ${phone}, Operator: ${operator}, Amount: ${amount}`);

  // সফল রেসপন্স পাঠানো হচ্ছে
  res.json({
     status: 'success',
     message: `Recharge request successful for ${phone}`,
     transaction_id: 'TXN' + Math.floor(Math.random() * 1000000),
     amount: amount,
     operator: operator
  });
});

app.listen(port, () => {
  console.log(`Server listening on port ${port}`);
});
