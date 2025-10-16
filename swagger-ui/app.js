const express = require('express');
const app = express();
const swaggerUi = require('swagger-ui-express');

const internalApi = require('./resources/internal.json');
const publicApi = require('./resources/public.json');

const port = 80;

let options = {}

app.use('/', express.static('site'));
app.use('/internal', swaggerUi.serveFiles(internalApi, options), swaggerUi.setup(internalApi));
app.use('/public', swaggerUi.serveFiles(publicApi, options), swaggerUi.setup(publicApi));

app.listen(port, () => {
  console.log(`listening at localhost:${port}`);
});

