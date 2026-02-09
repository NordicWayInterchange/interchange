import https from "https";
import fs from "fs";
import path from "path";
const logger = require("../logger");

export const getTLSAgent = () => {
  const pfxName = process.env.PFX_KEY_FILENAME || "";
  const passphrase = process.env.PFX_PASSPHRASE;
  if (!pfxName || !passphrase) {
    logger.error(
      "Either the name of the key file or the passphrase is not set. Please set these to be able to communicate with the interchange"
    );
    return;
  }

  try {
    const pfx = fs.readFileSync(path.join(process.cwd(), pfxName));
    const options = {
      pfx,
      passphrase,
      rejectUnauthorized: false,
    };
    return new https.Agent(options);
  } catch (error) {
    logger.error({ error });
  }
};
