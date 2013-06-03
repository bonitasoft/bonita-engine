ALTER TABLE user_membership DROP CONSTRAINT fk_usr_mbshp_usrId
GO
ALTER TABLE user_membership DROP CONSTRAINT fk_usr_mbshp_rId
GO
ALTER TABLE user_membership DROP CONSTRAINT fk_usr_mbshp_gId
GO
DROP TABLE  user_membership
GO

ALTER TABLE p_metadata_val DROP CONSTRAINT fk_p_md_val_mdId
GO
ALTER TABLE p_metadata_val DROP CONSTRAINT fk_p_md_val_usrId
GO
DROP TABLE  p_metadata_val
GO

DROP TABLE  p_metadata_def
GO

DROP TABLE  user_
GO

DROP TABLE  role
GO

DROP TABLE  group_
GO