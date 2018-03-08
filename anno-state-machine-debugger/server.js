const express = require('express');

const app = express();
const port = process.env.PORT || 5000;

app.get('/api/version', (req, res) => {
  res.send({ express: 'Version 0.0.1' });
});

app.listen(port, () => console.log(`Listening on port ${port}`));
