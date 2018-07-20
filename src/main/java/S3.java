import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.SSECustomerKey;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.s3.transfer.Copy;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.IOUtils;

public class S3 {
	private static S3 instance = null;

	protected S3() {
	}

	public static S3 getInstance() {
		if (instance == null) {
			instance = new S3();
		}
		return instance;
	}

	private Properties loadProperties() {
		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream("config.properties");
			try {
				prop.load(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return prop;
	}

	private Properties prop = loadProperties();

	public AmazonS3 getS3Client(Boolean isV4SignerType) {
		String accessKey = prop.getProperty("access_key");
		String secretKey = prop.getProperty("access_secret");
		boolean issecure = Boolean.parseBoolean(prop.getProperty("is_secure"));

		AWSCredentialsProvider credentials = new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(accessKey, secretKey));
		EndpointConfiguration epConfig = new AwsClientBuilder.EndpointConfiguration(prop.getProperty("endpoint"),
				prop.getProperty("region"));
		ClientConfiguration clientConfig = new ClientConfiguration();
		if (isV4SignerType) {
			clientConfig.setSignerOverride("AWSS3V4SignerType");
		} else {
			clientConfig.setSignerOverride("S3SignerType");
		}
		if (issecure) {
			clientConfig.setProtocol(Protocol.HTTPS);
		} else {
			clientConfig.setProtocol(Protocol.HTTP);
		}

		clientConfig.setClientExecutionTimeout(600 * 1000);
		// clientConfig.setRequestTimeout(10 * 1000);
		clientConfig.withConnectionTimeout(600 * 1000);
		clientConfig.withSocketTimeout(600 * 1000);
		// Allow as many retries as possible until the client execution timeout expires
		clientConfig.setMaxErrorRetry(Integer.MAX_VALUE);

		System.out.printf("EP is_secure: %s - %b %n", prop.getProperty("endpoint"), issecure);

		AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(credentials)
				.withEndpointConfiguration(epConfig).withClientConfiguration(clientConfig).enablePathStyleAccess()
				.build();
		return s3client;
	}

	public String getPrefix() {
		String prefix;
		if (prop.getProperty("bucket_prefix") != null) {
			prefix = prop.getProperty("bucket_prefix");
		} else {
			prefix = "test-";
		}
		return prefix;
	}

	public String getBucketName(String prefix) {
		Random rand = new Random();
		int num = rand.nextInt(50);
		String randomStr = UUID.randomUUID().toString();

		return prefix + randomStr + num;
	}

	public String getBucketName() {
		String prefix = getPrefix();
		Random rand = new Random();
		int num = rand.nextInt(50);
		String randomStr = UUID.randomUUID().toString();

		return prefix + randomStr + num;
	}

	public String repeat(String str, int count) {
		if (count <= 0) {
			return "";
		}
		return new String(new char[count]).replace("\0", str);
	}

	public Boolean isEPSecure() {
		return Boolean.parseBoolean(prop.getProperty("is_secure"));
	}

	public int teradownRetries = 0;

	public void tearDown(AmazonS3 svc) {
		if (teradownRetries > 0) {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {

			}
		}
		try {
			System.out.printf("TEARDOWN %n");
			List<Bucket> buckets = svc.listBuckets(new ListBucketsRequest());
			System.out.printf("Buckets list size: %d %n", buckets.size());
			String prefix = getPrefix();

			for (Bucket b : buckets) {
				String bucket_name = b.getName();
				if (b.getName().startsWith(prefix)) {
					VersionListing version_listing = svc
							.listVersions(new ListVersionsRequest().withBucketName(bucket_name));
					while (true) {
						for (java.util.Iterator<S3VersionSummary> iterator = version_listing.getVersionSummaries()
								.iterator(); iterator.hasNext();) {
							S3VersionSummary vs = (S3VersionSummary) iterator.next();
							try {
								svc.deleteVersion(bucket_name, vs.getKey(), vs.getVersionId());
							} catch (AmazonServiceException e) {

							} catch (SdkClientException e) {

							}
						}
						if (version_listing.isTruncated()) {
							version_listing = svc.listNextBatchOfVersions(version_listing);
						} else {
							break;
						}
					}

					ObjectListing object_listing = svc.listObjects(b.getName());
					while (true) {
						for (java.util.Iterator<S3ObjectSummary> iterator = object_listing.getObjectSummaries()
								.iterator(); iterator.hasNext();) {
							S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
							System.out.printf("Deleting bucket/object: %s / %s %n", bucket_name, summary.getKey());
							try {
								svc.deleteObject(bucket_name, summary.getKey());
							} catch (AmazonServiceException e) {

							} catch (SdkClientException e) {

							}
						}
						if (object_listing.isTruncated()) {
							object_listing = svc.listNextBatchOfObjects(object_listing);
						} else {
							break;
						}
					}
					try {
						svc.deleteBucket(new DeleteBucketRequest(b.getName()));
						System.out.printf("Deleted bucket: %s  %n", bucket_name);
					} catch (AmazonServiceException e) {

					} catch (SdkClientException e) {

					}
				}
			}
		} catch (AmazonServiceException e) {

		} catch (SdkClientException e) {
			if (teradownRetries < 10) {
				++teradownRetries;
				tearDown(svc);
			}
		}
	}

	public String[] EncryptionSseCustomerWrite(AmazonS3 svc, int file_size) {

		String prefix = getPrefix();
		String bucket_name = getBucketName(prefix);
		String key = "key1";
		String data = repeat("testcontent", file_size);
		InputStream datastream = new ByteArrayInputStream(data.getBytes());

		svc.createBucket(bucket_name);

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(data.length());
		objectMetadata.setContentType("text/plain");
		objectMetadata.setHeader("x-amz-server-side-encryption-customer-key",
				"pO3upElrwuEXSoFwCfnZPdSsmt/xWeFa0N9KgDijwVs=");
		objectMetadata.setSSECustomerKeyMd5("DWygnHRtgiJ77HCm+1rvHw==");
		objectMetadata.setSSECustomerAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
		PutObjectRequest putRequest = new PutObjectRequest(bucket_name, key, datastream, objectMetadata);

		svc.putObject(putRequest);

		SSECustomerKey skey = new SSECustomerKey("pO3upElrwuEXSoFwCfnZPdSsmt/xWeFa0N9KgDijwVs=");
		GetObjectRequest getRequest = new GetObjectRequest(bucket_name, key);
		getRequest.withSSECustomerKey(skey);

		InputStream inputStream = svc.getObject(getRequest).getObjectContent();
		String rdata = null;
		try {
			rdata = IOUtils.toString(inputStream);
		} catch (IOException e) {
			// e.printStackTrace();
		}

		String arr[] = new String[2];
		arr[0] = data;
		arr[1] = rdata;

		return arr;
	}

	public Bucket createKeys(AmazonS3 svc, String[] keys) {
		String prefix = prop.getProperty("bucket_prefix");
		String bucket_name = getBucketName(prefix);
		Bucket bucket = svc.createBucket(bucket_name);

		for (String k : keys) {
			svc.putObject(bucket.getName(), k, k);
		}
		return bucket;
	}

	public CompleteMultipartUploadRequest multipartUploadLLAPI(AmazonS3 svc, String bucket, String key,
			long size, String filePath) {

		List<PartETag> partETags = new ArrayList<PartETag>();

		InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket, key);
		InitiateMultipartUploadResult initResponse = svc.initiateMultipartUpload(initRequest);

		File file = new File(filePath);
		long contentLength = file.length();
		long partSize = size;

		long filePosition = 0;
		for (int i = 1; filePosition < contentLength; i++) {
			partSize = Math.min(partSize, (contentLength - filePosition));
			UploadPartRequest uploadRequest = new UploadPartRequest().withBucketName(bucket).withKey(key)
					.withUploadId(initResponse.getUploadId()).withPartNumber(i).withFileOffset(filePosition)
					.withFile(file).withPartSize(partSize);

			partETags.add((PartETag) svc.uploadPart(uploadRequest).getPartETag());

			filePosition += partSize;
		}

		CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucket, key,
				initResponse.getUploadId(), (List<PartETag>) partETags);

		return compRequest;
	}

	public CompleteMultipartUploadRequest multipartCopyLLAPI(AmazonS3 svc, String dstbkt, String dstkey, String srcbkt,
			String srckey, long size) {

		InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(dstbkt, dstkey);
		InitiateMultipartUploadResult initResult = svc.initiateMultipartUpload(initiateRequest);
		GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(srcbkt, srckey);

		ObjectMetadata metadataResult = svc.getObjectMetadata(metadataRequest);
		long objectSize = metadataResult.getContentLength(); // in bytes

		long partSize = size;

		long bytePosition = 0;
		int partNum = 1;

		List<PartETag> partETags = new ArrayList<PartETag>();
		while (bytePosition < objectSize) {
			long lastByte = Math.min(bytePosition + partSize - 1, objectSize - 1);
			CopyPartRequest copyRequest = new CopyPartRequest().withDestinationBucketName(dstbkt)
					.withDestinationKey(dstkey).withSourceBucketName(srcbkt).withSourceKey(srckey)
					.withUploadId(initResult.getUploadId()).withFirstByte(bytePosition)
					.withLastByte(lastByte)
					.withPartNumber(partNum++);

			CopyPartResult res = svc.copyPart(copyRequest);
			partETags.add(res.getPartETag());
			bytePosition += partSize;
		}
		CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(dstbkt, dstkey,
				initResult.getUploadId(), partETags);

		return completeRequest;
	}

	static List<PartETag> GetETags(List<CopyPartResult> responses) {
		List<PartETag> etags = new ArrayList<PartETag>();
		for (CopyPartResult response : responses) {
			etags.add(new PartETag(response.getPartNumber(), response.getETag()));
		}
		return etags;
	}

	public void waitForCompletion(Transfer xfer) {
		try {
			xfer.waitForCompletion();
		} catch (AmazonServiceException e) {
			// e.printStackTrace();
		} catch (AmazonClientException e) {
			// e.printStackTrace();
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}

	public Copy multipartCopyHLAPI(AmazonS3 svc, String dstbkt, String dstkey, String srcbkt, String srckey) {
		TransferManager tm = TransferManagerBuilder.standard().withS3Client(svc).build();
		Copy copy = tm.copy(srcbkt, srckey, dstbkt, dstkey);
		try {
			waitForCompletion(copy);
		} catch (AmazonServiceException e) {

		}
		return copy;
	}

	public Download downloadHLAPI(AmazonS3 svc, String bucket, String key, File file) {
		TransferManager tm = TransferManagerBuilder.standard().withS3Client(svc).build();
		Download download = tm.download(bucket, key, file);
		try {
			waitForCompletion(download);
		} catch (AmazonServiceException e) {

		}
		return download;
	}

	public MultipleFileDownload multipartDownloadHLAPI(AmazonS3 svc, String bucket, String key, File dstDir) {
		TransferManager tm = TransferManagerBuilder.standard().withS3Client(svc).build();
		MultipleFileDownload download = tm.downloadDirectory(bucket, key, dstDir);
		try {
			waitForCompletion(download);
		} catch (AmazonServiceException e) {

		}
		return download;
	}

	public Upload UploadFileHLAPI(AmazonS3 svc, String bucket, String key, String filePath) {
		TransferManager tm = TransferManagerBuilder.standard().withS3Client(svc).build();
		Upload upload = tm.upload(bucket, key, new File(filePath));
		try {
			waitForCompletion(upload);
		} catch (AmazonServiceException e) {

		}
		return upload;
	}

	public MultipleFileUpload UploadFileListHLAPI(AmazonS3 svc, String bucket, String key)
			throws AmazonServiceException, AmazonClientException, InterruptedException {

		ArrayList<File> files = new ArrayList<File>();

		String fname1 = "./data/file.mpg";
		String fname2 = "./data/sample.txt";
		createFile(fname1, 23 * 1024 * 1024);
		createFile(fname2, 256 * 1024);
		files.add(new File(fname1));
		files.add(new File(fname2));

		TransferManager tm = TransferManagerBuilder.standard().withS3Client(svc).build();
		MultipleFileUpload xfer = tm.uploadFileList(bucket, key, new File("."), files);
		try {
			waitForCompletion(xfer);
		} catch (AmazonServiceException e) {

		}
		return xfer;
	}

	public Transfer multipartUploadHLAPI(AmazonS3 svc, String bucket, String s3target, String directory)
			throws AmazonServiceException, AmazonClientException, InterruptedException {

		TransferManager tm = TransferManagerBuilder.standard().withS3Client(svc).build();
		Transfer t = tm.uploadDirectory(bucket, s3target, new File(directory), false);
		try {
			waitForCompletion(t);
		} catch (AmazonServiceException e) {

		}
		return t;
	}

	public void createFile(String fname, int size) {
		Random rand = new Random();
		byte[] myByteArray = new byte[size];
		rand.nextBytes(myByteArray);
		try {
			File f = new File(fname);
			if (f.exists() && !f.isDirectory()) {
				f.delete();
			}
			FileOutputStream fos = new FileOutputStream(fname);
			fos.write(myByteArray);
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

}
