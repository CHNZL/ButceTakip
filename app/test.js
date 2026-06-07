const https = require('https');

https.get('https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri/', (res) => {
    let rawData = '';
    res.on('data', (chunk) => { rawData += chunk; });
    res.on('end', () => {
        const fs = require('fs');
        fs.writeFileSync('yk.html', rawData);
    });
});
