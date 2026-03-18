/* SOURCES
 * https://github.com/PeculiarVentures/PKI.js/
 * https://github.com/PeculiarVentures/PKI.js/blob/master/examples/PKCS10ComplexExample/es6.ts
 * https://pkijs.org/examples/PKCS10ComplexExample/PKCS10_complex_example.html
 * https://gist.github.com/dmikey/4c6872211f53adf0b52de9c049f45495
 * */

import * as asn1js from "asn1js";
import {
  getCrypto,
  getAlgorithmParameters,
  CertificationRequest,
  AttributeTypeAndValue,
} from "pkijs/build";
import { arrayBufferToString, toBase64 } from "pvutils";
const hashAlg = "SHA-256";
const signAlg = "ECDSA";

export async function createPKCS10({ commonName, organization, country }) {
  const crypto = getWebCrypto();

  const keyPair = await generateKeyPair(crypto, getAlgorithm(signAlg, hashAlg));

  return {
    csr: toBase64(
      `-----BEGIN CERTIFICATE REQUEST-----\n${formatPEM(
        toBase64(
          arrayBufferToString(
            await createCSR(keyPair, hashAlg, {
              commonName,
              organization,
              country,
            })
          )
        )
      )}\n-----END CERTIFICATE REQUEST-----`
    ),
    privateKey: `-----BEGIN PRIVATE KEY-----\n${toBase64(
      arrayBufferToString(await crypto.exportKey("pkcs8", keyPair.privateKey))
    )}\n-----END PRIVATE KEY-----`,
  };
}

async function createCSR(
  keyPair,
  hashAlg,
  { commonName, organization, country }
) {
  const pkcs10 = new CertificationRequest();
  pkcs10.version = 0;

  pkcs10.subject.typesAndValues.push(
    new AttributeTypeAndValue({
      type: "2.5.4.6", //countryName
      value: new asn1js.PrintableString({ value: country }),
    })
  );
  pkcs10.subject.typesAndValues.push(
    new AttributeTypeAndValue({
      type: "2.5.4.10", //organizationName
      value: new asn1js.Utf8String({ value: organization }),
    })
  );
  pkcs10.subject.typesAndValues.push(
    new AttributeTypeAndValue({
      type: "2.5.4.3", //commonName
      value: new asn1js.Utf8String({ value: commonName }),
    })
  );

  fixSubject(pkcs10.subject);

  pkcs10.attributes = [];

  await pkcs10.subjectPublicKeyInfo.importKey(keyPair.publicKey);

  await pkcs10.sign(keyPair.privateKey, hashAlg);

  return pkcs10.toSchema().toBER(false);
}

function formatPEM(pemString) {
  return pemString.replace(/(.{64})/g, "$1\n");
}

function getWebCrypto() {
  const crypto = getCrypto();
  if (typeof crypto === "undefined") throw "No WebCrypto extension found";
  return crypto;
}

/**
 * @Description Temporary solution for wrong parsing of subject
 * @param subject - PKCS#10 Subject
 */
function fixSubject(subject) {
  if (subject.typesAndValues) {
    const schema = new asn1js.Sequence({
      value: Array.from(
        subject.typesAndValues,
        (element) =>
          new asn1js.Set({
            value: [element.toSchema()],
          })
      ),
    });
    const der = schema.toBER();
    subject.fromSchema(asn1js.fromBER(der).result);
  }
}

function getAlgorithm(signAlg, hashAlg) {
  const algorithm = getAlgorithmParameters(signAlg, "generatekey");
  if ("hash" in algorithm.algorithm) algorithm.algorithm.hash.name = hashAlg;
  return algorithm;
}

function generateKeyPair(crypto, algorithm) {
  return crypto.generateKey(algorithm.algorithm, true, algorithm.usages);
}
