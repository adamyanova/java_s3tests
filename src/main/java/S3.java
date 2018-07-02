import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.testng.SkipException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
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
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.StringUtils;

public class S3 {
	
	private static S3 instance = null;
	
	protected S3() {
	      
	}
	
	public static S3 getInstance() {
	  if(instance == null) {
	         instance = new S3();
	   }
	   return instance;
	}
	
	static Properties prop = new Properties();
	InputStream input = null;
	
	AmazonS3 svc = getCLI();
	
	public AmazonS3 getCLI() {
		
		try {
			input = new FileInputStream("config.properties");
			try {
				prop.load(input);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String endpoint = prop.getProperty("endpoint");
		String accessKey = prop.getProperty("access_key");
		String secretKey = prop.getProperty("access_secret");
		Boolean issecure = Boolean.parseBoolean(prop.getProperty("is_secure"));
		
		System.out.printf(" JAVA Config is: ");
		System.out.printf(" %s", endpoint);
		System.out.printf(" %s", accessKey);
		System.out.printf(" %s", secretKey);
		System.out.printf(" %b", issecure);

		AmazonS3 svc = getConn(endpoint, accessKey, secretKey, issecure);

		return svc;
	}
	
	public AmazonS3 getAWS4CLI() {
		
		try {
			input = new FileInputStream("config.properties");
			try {
				prop.load(input);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String endpoint = prop.getProperty("endpoint");
		String accessKey = prop.getProperty("access_key");
		String secretKey = prop.getProperty("access_secret");
		Boolean issecure = Boolean.parseBoolean(prop.getProperty("is_secure"));
		
		AmazonS3 svc = getAWS4Conn(endpoint, accessKey, secretKey, issecure);

		return svc;
	}
	
	public String getPrefix()
	{
		String prefix = "test";
		
		return prefix;
	}
	
	
	@SuppressWarnings("deprecation")
	public AmazonS3 getConn(String endpoint,String accessKey, String secretKey,Boolean issecure) {
		
		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setSignerOverride("AWSS3V2SignerType");
		AmazonS3ClientBuilder.standard().setEndpointConfiguration(
				new AwsClientBuilder.EndpointConfiguration(endpoint, "mexico"));
		
		if (issecure){
			clientConfig.setProtocol(Protocol.HTTP);
		}else {
			
			clientConfig.setProtocol(Protocol.HTTP);
		}
		
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		@SuppressWarnings("deprecation")
		AmazonS3 s3client = new AmazonS3Client(credentials); //

		s3client.setEndpoint(endpoint);

		s3client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
		
		
		
		return s3client;
	}
	
	@SuppressWarnings("deprecation")
	public AmazonS3 getAWS4Conn(String endpoint,String accessKey, String secretKey,Boolean issecure) {
		
		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setSignerOverride("AWSS3V4SignerType");
		AmazonS3ClientBuilder.standard().setEndpointConfiguration(
				new AwsClientBuilder.EndpointConfiguration(endpoint, "mexico"));
		
		if (issecure){
			clientConfig.setProtocol(Protocol.HTTP);
		}else {
			clientConfig.setProtocol(Protocol.HTTPS);
		}
		
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		@SuppressWarnings("deprecation")
		AmazonS3 s3client = new AmazonS3Client(credentials, clientConfig); 

		s3client.setEndpoint(endpoint);

		s3client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
		
		return s3client;
	}
	
	public String getBucketName(String prefix) {
		
		Random rand = new Random(); 
		int num = rand.nextInt(50);
		String randomStr = UUID.randomUUID().toString();
		
		return prefix + randomStr + num;
	}
	
	public String getBucketName() {
		String prefix = prop.getProperty("bucket_prefix");
		
		Random rand = new Random(); 
		int num = rand.nextInt(50);
		String randomStr = UUID.randomUUID().toString();
		
		return prefix + randomStr + num;
	}
	public String repeat(String str, int count){
	    if(count <= 0) {return "";}
	    return new String(new char[count]).replace("\0", str);
	}
	
	public void tearDown(AmazonS3 svc) {
		
		java.util.List<Bucket> buckets = svc.listBuckets();
		String prefix = getPrefix();
		
		for (Bucket b : buckets) {
			
			String bucket_name = b.getName();
			
			if (b.getName().startsWith(prefix)) {
				
				ObjectListing object_listing = svc.listObjects(b.getName());
				while (true) {
	                for (java.util.Iterator<S3ObjectSummary> iterator =
	                        object_listing.getObjectSummaries().iterator();
	                        iterator.hasNext();) {
	                    S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
	                    svc.deleteObject(bucket_name, summary.getKey());
	                }

	                if (object_listing.isTruncated()) {
	                    object_listing = svc.listNextBatchOfObjects(object_listing);
	                } else {
	                    break;
	                }
	            };
	            
	            VersionListing version_listing = svc.listVersions(
	                    new ListVersionsRequest().withBucketName(bucket_name));
	            while (true) {
	                for (java.util.Iterator<S3VersionSummary> iterator =
	                        version_listing.getVersionSummaries().iterator();
	                        iterator.hasNext();) {
	                    S3VersionSummary vs = (S3VersionSummary)iterator.next();
	                    svc.deleteVersion(
	          
	                  bucket_name, vs.getKey(), vs.getVersionId());
	                }

	                if (version_listing.isTruncated()) {
	                    version_listing = svc.listNextBatchOfVersions(
	                            version_listing);
	                } else {
	                    break;
	                }
	            }
			    svc.deleteBucket(new DeleteBucketRequest(b.getName()));
			}
		}
	}
	
	private static SecretKey generateSecretKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
	
	public String[] EncryptionSseCustomerWrite(AmazonS3 svc, int file_size) {
		
		String prefix = prop.getProperty("bucket_prefix");
		String bucket_name = getBucketName(prefix);
		String key ="key1";
		String data = repeat("testcontent", file_size);
		
		svc.createBucket(bucket_name);	
		
		SecretKey secretKey = generateSecretKey();
        SSECustomerKey sseKey = new SSECustomerKey(secretKey);
		
		PutObjectRequest putRequest = new PutObjectRequest(bucket_name, key, data).withSSECustomerKey(sseKey);
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(data.length());
		objectMetadata.setHeader("x-amz-server-side-encryption-customer-key", "pO3upElrwuEXSoFwCfnZPdSsmt/xWeFa0N9KgDijwVs=");
		objectMetadata.setHeader("x-amz-server-side-encryption-customer-key-md5", "DWygnHRtgiJ77HCm+1rvHw==");
		putRequest.setMetadata(objectMetadata);
		svc.putObject(putRequest);
		
		
		String rdata = svc.getObjectAsString(bucket_name, key);
		
		//trying to return two values
		String arr[] = new String[2];
        arr[0]= data;
        arr[1] =  rdata;
		
		return arr;
				
	}
	
	public String[] EncryptionSseKMSCustomerWrite(AmazonS3 svc, int file_size, String keyId) {
		
		String prefix = prop.getProperty("bucket_prefix");
		String bucket_name = getBucketName(prefix);
		String key ="key1";
		String data = repeat("testcontent", file_size);
		
		if (keyId == "") {
			keyId = "testkey-1";
		}
		
		
		svc.createBucket(bucket_name);	
		
		PutObjectRequest putRequest = new PutObjectRequest(bucket_name, key, data);
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(file_size);
		objectMetadata.setHeader("x-amz-server-side-encryption", "aws:kms");
		objectMetadata.setHeader("x-amz-server-side-encryption-aws-kms-key-id", keyId );
		putRequest.setMetadata(objectMetadata);
		svc.putObject(putRequest);
		
		
		String rdata = svc.getObjectAsString(bucket_name, key);
		
		//trying to return two values
		String arr[] = new String[2];
        arr[0]= data;
        arr[1] =  rdata;
		
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
	
	public ObjectMetadata getSetMetadata(AmazonS3 svc, String bucket_name, String metadata) {
		
		String content = "testcontent";
		String key = "key1";
		
		svc.createBucket(bucket_name);
		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata mdata = new ObjectMetadata();
		mdata.setContentLength(contentBytes.length);
		mdata.addUserMetadata("Mymeta", metadata);
		
		svc.putObject(new PutObjectRequest(bucket_name, key, is, mdata));
		
		S3Object resp = svc.getObject(new GetObjectRequest(bucket_name, key));
		
		return resp.getObjectMetadata();
	}
	
	public <PartETag> CompleteMultipartUploadRequest multipartUploadLLAPI(AmazonS3 svc, String bucket, String key, long size, String filePath) {
		
		List<PartETag> partETags = new ArrayList<PartETag>();

		InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket, key);
		InitiateMultipartUploadResult initResponse = svc.initiateMultipartUpload(initRequest);

		File file = new File(filePath);
		long contentLength = file.length();
		long partSize = size;

		
			
		    long filePosition = 0;
		    for (int i = 1; filePosition < contentLength; i++) {
		    	
		    	partSize = Math.min(partSize, (contentLength - filePosition));
		    	
		        // Create request to upload a part.
		        UploadPartRequest uploadRequest = new UploadPartRequest()
		            .withBucketName(bucket).withKey(key)
		            .withUploadId(initResponse.getUploadId()).withPartNumber(i)
		            .withFileOffset(filePosition)
		            .withFile(file)
		            .withPartSize(partSize);
		        
		        partETags.add((PartETag) svc.uploadPart(uploadRequest).getPartETag());

		        filePosition += partSize;
		    }

		    CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucket, key, 
		                                               initResponse.getUploadId(), 
		                                               (List<com.amazonaws.services.s3.model.PartETag>) partETags);
		    
		    return compRequest;
		    
	}
	
	public CompleteMultipartUploadRequest multipartCopyLLAPI(AmazonS3 svc,String dstbkt, String dstkey, String srcbkt, String srckey,long size) {
		
		List<CopyPartResult> copyResponses =new ArrayList<CopyPartResult>();
		
		InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(dstbkt, dstkey);
	        
	    InitiateMultipartUploadResult initResult = svc.initiateMultipartUpload(initiateRequest);

	      
	    GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(srcbkt, srckey);

	            ObjectMetadata metadataResult = svc.getObjectMetadata(metadataRequest);
	            long objectSize = metadataResult.getContentLength(); // in bytes

	            // Copy parts.
	            long partSize = 5 * (long)Math.pow(2.0, 20.0); // 5 MB

	            long bytePosition = 0;
	            for (int i = 1; bytePosition < objectSize; i++)
	            {
	            	CopyPartRequest copyRequest = new CopyPartRequest()
	                   .withDestinationBucketName(dstbkt)
	                   .withDestinationKey(dstkey)
	                   .withSourceBucketName(srcbkt)
	                   .withSourceKey(srckey)
	                   .withUploadId(initResult.getUploadId())
	                   .withFirstByte(bytePosition)
	                   .withLastByte(bytePosition + partSize -1 >= objectSize ? objectSize - 1 : bytePosition + partSize - 1) 
	                   .withPartNumber(i);

	                copyResponses.add(svc.copyPart(copyRequest));
	                bytePosition += partSize;

	            }
	            CompleteMultipartUploadRequest completeRequest = new 
	            	CompleteMultipartUploadRequest(
	            			dstbkt,
	            			dstkey,
	            			initResult.getUploadId(),
	            			GetETags(copyResponses));

	            
	        
	        
	        return completeRequest;
	     
	}
	
	static List<com.amazonaws.services.s3.model.PartETag> GetETags(List<CopyPartResult> responses)
    {
        List<com.amazonaws.services.s3.model.PartETag> etags = new ArrayList<com.amazonaws.services.s3.model.PartETag>();
        for (CopyPartResult response : responses)
        {
            etags.add(new com.amazonaws.services.s3.model.PartETag(response.getPartNumber(), response.getETag()));
        }
        return etags;
    } 
	
	public Copy multipartCopyHLAPI(AmazonS3 svc,String dstbkt, String dstkey, String srcbkt, String srckey) {
	
		 @SuppressWarnings("deprecation")
			TransferManager tm = new TransferManager(svc);  
	        Copy copy = tm.copy(srcbkt, srckey, dstbkt, dstkey);
	        try {
				try {
					copy.waitForCompletion();
				} catch (InterruptedException e) {
					
				}
			} catch (AmazonClientException amazonClientException) {
				
				
			}
			
	        return copy;
			
	}
	
	public Download downloadHLAPI(AmazonS3 svc,String bucket, String key, File file) {
		
		 @SuppressWarnings("deprecation")
			TransferManager tm = new TransferManager(svc);  
	        Download download = tm.download(bucket, key, file);
	        try {
				try {
					download.waitForCompletion();
				} catch (InterruptedException e) {
					
				}
			} catch (AmazonClientException amazonClientException) {
				
				
			}
			
	        return download;	
	}
	
	public MultipleFileDownload multipartDownloadHLAPI(AmazonS3 svc,String bucket, String key, File dstDir) {
		
		 @SuppressWarnings("deprecation")
		TransferManager tm = new TransferManager(svc);  
		 	MultipleFileDownload download = tm.downloadDirectory(bucket, key, dstDir);
	        try {
				try {
					download.waitForCompletion();
				} catch (InterruptedException e) {
					
				}
			} catch (AmazonClientException amazonClientException) {
				
				
			}
			
	        return download;	
	}
	
	public Upload UploadFileHLAPI(AmazonS3 svc,String bucket, String key, String filePath) { 
        
        @SuppressWarnings("deprecation")
		TransferManager tm = new TransferManager(svc);  
        Upload upload = tm.upload(bucket, key, new File(filePath));
        try {
			try {
				upload.waitForCompletion();
			} catch (InterruptedException e) {
				
			}
		} catch (AmazonClientException amazonClientException) {
			
			
		}
		
        return upload;
	}
	
	public MultipleFileUpload UploadFileListHLAPI(AmazonS3 svc, String bucket, String key) throws AmazonServiceException, AmazonClientException, InterruptedException { 
        
	    ArrayList<File> files = new ArrayList<File>();
	    files.add(new File("./data/file.mpg"));
	    files.add(new File("./data/sample.txt"));
	    
	    TransferManager xfer_mgr = new TransferManager(svc);
	    MultipleFileUpload xfer = xfer_mgr.uploadFileList(bucket, key, new File("."), files);
	    xfer.waitForCompletion();
	    
	    return xfer;
	}
	
	public Transfer multipartUploadHLAPI(AmazonS3 svc, String bucket, String s3target, String directory) throws AmazonServiceException, AmazonClientException, InterruptedException { 
        
		@SuppressWarnings("deprecation")
		TransferManager tm = new TransferManager(svc);
		Transfer t = tm.uploadDirectory(bucket, s3target, new File(directory), false);
	    try {
	      t.waitForCompletion();
	    } finally {
	      tm.shutdownNow(false);
	    }
	    
	    return t;
		
	}
	
}
