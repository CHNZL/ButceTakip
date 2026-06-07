const https = require('https');

https.get('https://www.yapikredi.com.tr/api/yatirimciKosesi/dovizKurlari', (res) => {
    let rawData = '';
    res.on('data', (chunk) => { rawData += chunk; });
    res.on('end', () => {
        console.log("Response dovizKurlari:");
        console.log(rawData.substring(0, 1000));
    });
}).on('error', (e) => {
    console.error(`Got error: ${e.message}`);
});

https.get('https://www.yapikredi.com.tr/api/stockmarket/currencies', (res) => {
    let rawData = '';
    res.on('data', (chunk) => { rawData += chunk; });
    res.on('end', () => {
        console.log("Response currencies:");
        console.log(rawData.substring(0, 1000));
    });
}).on('error', (e) => {
    console.error(`Got error: ${e.message}`);
});
