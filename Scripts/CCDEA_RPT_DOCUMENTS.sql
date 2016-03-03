
create or replace force view "CCDEA_RPT_DOCUMENTS"
(
    r_object_id,
    s_reg_branch_code,
    s_doc_source_id,
	s_doc_source_code,
    s_doc_type,
    s_customer_number,
    s_customer_name,
    s_doc_number,
    t_doc_date,
    t_receive_date,
    t_accept_reject_column,
    s_state,
    s_state_comment,
    t_close_date,
    s_close_reason,
    id_dossier,
    b_is_aml,
    t_last_change_date,
    s_last_change_author,
    r_creation_date,
	s_passport_number,
	s_contract_number,
	t_contract_date,
	s_passport_type_code,
	s_author_department_code,
	s_doc_channel_name,
	s_doc_channel_ref_id,
	s_content_url,
	b_no_amount,
	d_amount,
	t_contract_end_date,
	s_contract_currency_code,
	t_origin_date,
	s_currency_code,
	s_doc_kind_code,
	s_kvalp,
	d_summap,
	s_kvalk,
	d_summak,
	b_priz_post,
	s_kstrana,
	s_state_code,
	b_is_changed,
	s_bank_name,
	s_bank_country_code,
	s_account,
	b_is_urgent,
	b_is_vip,
	t_detail_date,
	s_vo_code,
	s_payment_number,
	t_payment_date,
	b_is_payment,
	t_valid_date,
	b_is_payment_advance,
	b_is_payment_delay,
	b_is_closed,
	t_expire_date,
	s_archive_dossier_number,
	b_is_third_party,
	b_report402,
	b_report405,
	b_report406,
	b_no_general_amount,
	d_general_amount,
	d_general_payment_amount,
	s_general_currency_code,
	b_is_reorganization,
	s_reorganization_info,
	d_customer_capital,
	t_customer_reg_date,
	s_shipping_country_code,
	s_shipper_country_code,
	s_port_country_code,
	s_ship_name,
	s_third_country_code,
	s_third_bank_country_code,
	s_comment
)
as

select
    r_object_id,
    s_reg_branch_code,
    s_doc_source_id,
	s_doc_source_code,
    'Паспорт сделки' as s_doc_type,
    s_customer_number,
    s_customer_name,
    s_passport_number as s_doc_number,
    CASE WHEN t_passport_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_passport_date 
    END as t_doc_date,
    CASE WHEN t_receive_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_receive_date 
    END as t_receive_date,
    null as t_accept_reject_column,
    null as s_state,
    null as s_state_comment,
    CASE WHEN t_close_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_close_date 
    END as t_close_date,
    s_close_reason,
    id_dossier,
    null as b_is_aml,
    CASE WHEN t_last_change_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_last_change_date 
    END as t_last_change_date,
    s_last_change_author,
    r_creation_date,
	s_passport_number,
	s_contract_number,
	CASE WHEN t_contract_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_date 
    END as t_contract_date,
	s_passport_type_code,
	s_author_department_code,
	s_doc_channel_name,
	s_doc_channel_ref_id,
	s_content_url,
	b_no_amount,
	d_amount,
	CASE WHEN t_contract_end_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_end_date 
    END as t_contract_end_date,
	s_contract_currency_code,
	null as t_origin_date,
	null as s_currency_code,
	null as s_doc_kind_code,
	null as s_kvalp,
	null as d_summap,
	null as s_kvalk,
	null as d_summak,
	null as b_priz_post,
	null as s_kstrana,
	null as s_state_code,
	null as b_is_changed,
	null as s_bank_name,
	null as s_bank_country_code,
	null as s_account,
	null as b_is_urgent,
	null as b_is_vip,
	null as t_detail_date,
	null as s_vo_code,
	null as s_payment_number,
	null as t_payment_date,
	null as b_is_payment,
	null as t_valid_date,
	null as b_is_payment_advance,
	null as b_is_payment_delay,
	null as b_is_closed,
	null as t_expire_date,
	null as s_archive_dossier_number,
	null as b_is_third_party,
	null as b_report402,
	null as b_report405,
	null as b_report406,
	null as b_no_general_amount,
	null as d_general_amount,
	null as d_general_payment_amount,
	null as s_general_currency_code,
	null as b_is_reorganization,
	null as s_reorganization_info,
	null as d_customer_capital,
	null as t_customer_reg_date,
	null as s_shipping_country_code,
	null as s_shipper_country_code,
	null as s_port_country_code,
	null as s_ship_name,
	null as s_third_country_code,
	null as s_third_bank_country_code,
	null as s_comment
from
    ccdea_passport_sp
union
select
    r_object_id,
    s_reg_branch_code,
    s_doc_source_id,
	s_doc_source_code,
    'ВБК' as s_doc_type,
    s_customer_number,
    s_customer_name,
    null as s_doc_number,
    null as t_doc_date,
    null as t_receive_date,
    null as t_accept_reject_column,
    null as s_state,
    null as s_state_comment,
    CASE WHEN t_close_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_close_date 
    END as t_close_date,
    s_close_reason,
    id_dossier,
    null as b_is_aml,
    CASE WHEN t_last_change_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_last_change_date 
    END as t_last_change_date,
    s_last_change_author,
    r_creation_date,
	s_passport_number,
	s_contract_number,
	CASE WHEN t_contract_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_date 
    END as t_contract_date,
	s_passport_type_code,
	s_author_department_code,
	s_doc_channel_name,
	s_doc_channel_ref_id,
	s_content_url,
	b_no_amount,
	d_amount,
	CASE WHEN t_contract_end_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_end_date 
    END as t_contract_end_date,
	s_contract_currency_code,
	null as t_origin_date,
	null as s_currency_code,
	null as s_doc_kind_code,
	null as s_kvalp,
	null as d_summap,
	null as s_kvalk,
	null as d_summak,
	null as b_priz_post,
	null as s_kstrana,
	null as s_state_code,
	null as b_is_changed,
	null as s_bank_name,
	null as s_bank_country_code,
	null as s_account,
	null as b_is_urgent,
	null as b_is_vip,
	null as t_detail_date,
	null as s_vo_code,
	null as s_payment_number,
	null as t_payment_date,
	null as b_is_payment,
	null as t_valid_date,
	null as b_is_payment_advance,
	null as b_is_payment_delay,
	null as b_is_closed,
	null as t_expire_date,
	null as s_archive_dossier_number,
	null as b_is_third_party,
	null as b_report402,
	null as b_report405,
	null as b_report406,
	null as b_no_general_amount,
	null as d_general_amount,
	null as d_general_payment_amount,
	null as s_general_currency_code,
	null as b_is_reorganization,
	null as s_reorganization_info,
	null as d_customer_capital,
	null as t_customer_reg_date,
	null as s_shipping_country_code,
	null as s_shipper_country_code,
	null as s_port_country_code,
	null as s_ship_name,
	null as s_third_country_code,
	null as s_third_bank_country_code,
	null as s_comment
from
    ccdea_vbk_sp
union
select
    r_object_id,
    s_reg_branch_code,
    s_doc_source_id,
	s_doc_source_code,
    s_doc_type,
    s_customer_number,
    s_customer_name,
    s_contract_number as s_doc_number,
    CASE WHEN t_contract_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_date 
    END as t_doc_date,
    CASE WHEN t_receive_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_receive_date 
    END as t_receive_date,
    null as t_accept_reject_column,
    s_state,
    s_state_comment,
    CASE WHEN t_close_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_close_date 
    END as t_close_date,
    null as s_close_reason,
    id_dossier,
    b_is_aml,
    CASE WHEN t_last_change_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_last_change_date 
    END as t_last_change_date,
    s_last_change_author,
    r_creation_date,
	s_passport_number,
	s_contract_number,
	CASE WHEN t_contract_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_date 
    END as t_contract_date,
	s_passport_type_code,
	s_author_department_code,
	s_doc_channel_name,
	s_doc_channel_ref_id,
	s_content_url,
	null as b_no_amount,
	d_amount,
	null as t_contract_end_date,
	null as s_contract_currency_code,
	null as t_origin_date,
	s_currency_code,
	null as s_doc_kind_code,
	null as s_kvalp,
	null as d_summap,
	null as s_kvalk,
	null as d_summak,
	null as b_priz_post,
	null as s_kstrana,
	null as s_state_code,
	null as b_is_changed,
	null as s_bank_name,
	null as s_bank_country_code,
	null as s_account,
	b_is_urgent,
	b_is_vip,
	null as t_detail_date,
	null as s_vo_code,
	null as s_payment_number,
	null as t_payment_date,
	null as b_is_payment,
	null as t_valid_date,
	b_is_payment_advance,
	b_is_payment_delay,
	b_is_closed,
	CASE WHEN t_expire_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_expire_date 
    END as t_expire_date,
	s_archive_dossier_number,
	b_is_third_party,
	b_report402,
	b_report405,
	b_report406,
	b_no_general_amount,
	d_general_amount,
	d_general_payment_amount,
	s_general_currency_code,
	b_is_reorganization,
	s_reorganization_info,
	d_customer_capital,
	CASE WHEN t_customer_reg_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_customer_reg_date 
    END as t_customer_reg_date,
	s_shipping_country_code,
	s_shipper_country_code,
	s_port_country_code,
	s_ship_name,
	s_third_country_code,
	s_third_bank_country_code,
	s_comment
from
    ccdea_contract_sp
union
select
    r_object_id,
    s_reg_branch_code,
    s_doc_source_id,
	s_doc_source_code,
    s_doc_type,
    s_customer_number,
    s_customer_name,
    s_doc_number,
    CASE WHEN t_doc_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_doc_date 
    END as t_doc_date,
    CASE WHEN t_receive_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_receive_date 
    END as t_receive_date,
    CASE WHEN t_accept_reject_column = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_accept_reject_column 
    END as t_accept_reject_column,
    s_state,
    s_state_comment,
    null as t_close_date,
    null as s_close_reason,
    id_dossier,
    null as b_is_aml,
    CASE WHEN t_last_change_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_last_change_date 
    END as t_last_change_date,
    s_last_change_author,
    r_creation_date,
	s_passport_number,
	s_contract_number,
	CASE WHEN t_contract_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_date 
    END as t_contract_date,
	s_passport_type_code,
	s_author_department_code,
	s_doc_channel_name,
	s_doc_channel_ref_id,
	s_content_url,
	null as b_no_amount,
	d_amount,
	null as t_contract_end_date,
	null as s_contract_currency_code,
	null as t_origin_date,
	s_currency_code,
	null as s_doc_kind_code,
	null as s_kvalp,
	null as d_summap,
	null as s_kvalk,
	null as d_summak,
	null as b_priz_post,
	null as s_kstrana,
	s_state_code,
	b_is_changed,
	s_bank_name,
	null as s_bank_country_code,
	null as s_account,
	b_is_urgent,
	b_is_vip,
	null as t_detail_date,
	null as s_vo_code,
	null as s_payment_number,
	null as t_payment_date,
	null as b_is_payment,
	null as t_valid_date,
	null as b_is_payment_advance,
	null as b_is_payment_delay,
	null as b_is_closed,
	null as t_expire_date,
	null as s_archive_dossier_number,
	null as b_is_third_party,
	null as b_report402,
	null as b_report405,
	null as b_report406,
	null as b_no_general_amount,
	null as d_general_amount,
	null as d_general_payment_amount,
	null as s_general_currency_code,
	null as b_is_reorganization,
	null as s_reorganization_info,
	null as d_customer_capital,
	null as t_customer_reg_date,
	null as s_shipping_country_code,
	null as s_shipper_country_code,
	null as s_port_country_code,
	null as s_ship_name,
	null as s_third_country_code,
	null as s_third_bank_country_code,
	null as s_comment
from
    ccdea_spd_sp
union
select
    r_object_id,
    s_reg_branch_code,
    s_doc_source_id,
	s_doc_source_code,
    'СВО' as s_doc_type,
    s_customer_number,
    s_customer_name,
    s_doc_number,
    CASE WHEN t_doc_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_doc_date 
    END as t_doc_date,
    CASE WHEN t_receive_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_receive_date 
    END as t_receive_date,
    CASE WHEN t_accept_reject_column = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_accept_reject_column 
    END as t_accept_reject_column,
    s_state,
    s_state_comment,
    null as t_close_date,
    null as s_close_reason,
    id_dossier,
    null as b_is_aml,
    CASE WHEN t_last_change_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_last_change_date 
    END as t_last_change_date,
    s_last_change_author,
    r_creation_date,
	s_passport_number,
	s_contract_number,
	CASE WHEN t_contract_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_date 
    END as t_contract_date,
	s_passport_type_code,
	s_author_department_code,
	s_doc_channel_name,
	s_doc_channel_ref_id,
	s_content_url,
	null as b_no_amount,
	d_payment_amount as d_amount,
	null as t_contract_end_date,
	null as s_contract_currency_code,
	null as t_origin_date,
	s_payment_currency_code as s_currency_code,
	null as s_doc_kind_code,
	null as s_kvalp,
	null as d_summap,
	null as s_kvalk,
	null as d_summak,
	null as b_priz_post,
	null as s_kstrana,
	s_state_code,
	b_is_changed,
	s_bank_name,
	s_bank_country_code,
	s_account,
	b_is_urgent,
	b_is_vip,
	CASE WHEN t_detail_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_detail_date 
    END as t_detail_date,
	s_vo_code,
	s_payment_number,
	CASE WHEN t_payment_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_payment_date 
    END as t_payment_date,
	b_is_payment,
	CASE WHEN t_valid_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_valid_date 
    END as t_valid_date,
	null as b_is_payment_advance,
	null as b_is_payment_delay,
	null as b_is_closed,
	null as t_expire_date,
	null as s_archive_dossier_number,
	null as b_is_third_party,
	null as b_report402,
	null as b_report405,
	null as b_report406,
	null as b_no_general_amount,
	null as d_general_amount,
	null as d_general_payment_amount,
	null as s_general_currency_code,
	null as b_is_reorganization,
	null as s_reorganization_info,
	null as d_customer_capital,
	null as t_customer_reg_date,
	null as s_shipping_country_code,
	null as s_shipper_country_code,
	null as s_port_country_code,
	null as s_ship_name,
	null as s_third_country_code,
	null as s_third_bank_country_code,
	s_remarks as s_comment
from
    ccdea_svo_detail_sp
union
select
    r_object_id,
    s_reg_branch_code,
    s_doc_source_id,
	s_doc_source_code,
    'Заявление' as s_doc_type,
    s_customer_number,
    s_customer_name,
    null as s_doc_number,
    null as t_doc_date,
    CASE WHEN t_receive_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_receive_date 
    END as t_receive_date,
    CASE WHEN t_accept_reject_column = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_accept_reject_column 
    END as t_accept_reject_column,
    s_state,
    s_state_comment,
    null as t_close_date,
    null as s_close_reason,
    id_dossier,
    null as b_is_aml,
    CASE WHEN t_last_change_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_last_change_date 
    END as t_last_change_date,
    s_last_change_author,
    r_creation_date,
	s_passport_number,
	s_contract_number,
	CASE WHEN t_contract_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_date 
    END as t_contract_date,
	s_passport_type_code,
	s_author_department_code,
	s_doc_channel_name,
	s_doc_channel_ref_id,
	s_content_url,
	null as b_no_amount,
	null as d_amount,
	null as t_contract_end_date,
	null as s_contract_currency_code,
	CASE WHEN t_origin_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_origin_date 
    END as t_origin_date,
	null as s_currency_code,
	null as s_doc_kind_code,
	null as s_kvalp,
	null as d_summap,
	null as s_kvalk,
	null as d_summak,
	null as b_priz_post,
	null as s_kstrana,
	null as s_state_code,
	null as b_is_changed,
	null as s_bank_name,
	null as s_bank_country_code,
	null as s_account,
	null as b_is_urgent,
	null as b_is_vip,
	null as t_detail_date,
	null as s_vo_code,
	null as s_payment_number,
	null as t_payment_date,
	null as b_is_payment,
	null as t_valid_date,
	null as b_is_payment_advance,
	null as b_is_payment_delay,
	null as b_is_closed,
	null as t_expire_date,
	null as s_archive_dossier_number,
	null as b_is_third_party,
	null as b_report402,
	null as b_report405,
	null as b_report406,
	null as b_no_general_amount,
	null as d_general_amount,
	null as d_general_payment_amount,
	null as s_general_currency_code,
	null as b_is_reorganization,
	null as s_reorganization_info,
	null as d_customer_capital,
	null as t_customer_reg_date,
	null as s_shipping_country_code,
	null as s_shipper_country_code,
	null as s_port_country_code,
	null as s_ship_name,
	null as s_third_country_code,
	null as s_third_bank_country_code,
	null as s_comment
from
    ccdea_request_sp
union
select
    r_object_id,
    s_reg_branch_code,
    s_doc_source_id,
	s_doc_source_code,
    s_doc_type,
    s_customer_number,
    s_customer_name,
    s_doc_number,
    CASE WHEN t_doc_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_doc_date 
    END as t_doc_date,
    null as t_receive_date,
    null as t_accept_reject_column,
    s_state,
    s_state_comment,
    null as t_close_date,
    null as s_close_reason,
    id_dossier,
    null as b_is_aml,
    CASE WHEN t_last_change_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_last_change_date 
    END as t_last_change_date,
    s_last_change_author,
    r_creation_date,
	s_passport_number,
	s_contract_number,
	CASE WHEN t_contract_date = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_contract_date 
    END as t_contract_date,
	s_passport_type_code,
	s_author_department_code,
	s_doc_channel_name,
	s_doc_channel_ref_id,
	s_content_url,
	null as b_no_amount,
	d_amount,
	null as t_contract_end_date,
	null as s_contract_currency_code,
	null as t_origin_date,
	s_currency_code,
	s_doc_kind_code,
	s_kvalp,
	d_summap,
	s_kvalk,
	d_summak,
	b_priz_post,
	s_kstrana,
	null as s_state_code,
	null as b_is_changed,
	null as s_bank_name,
	null as s_bank_country_code,
	null as s_account,
	null as b_is_urgent,
	null as b_is_vip,
	null as t_detail_date,
	null as s_vo_code,
	null as s_payment_number,
	null as t_payment_date,
	null as b_is_payment,
	CASE WHEN t_srok_wait = TO_DATE('0001-01-01','yyyy-MM-dd') THEN
      null 
    ELSE 
      t_srok_wait 
    END as t_valid_date,
	null as b_is_payment_advance,
	null as b_is_payment_delay,
	null as b_is_closed,
	null as t_expire_date,
	null as s_archive_dossier_number,
	null as b_is_third_party,
	null as b_report402,
	null as b_report405,
	null as b_report406,
	null as b_no_general_amount,
	null as d_general_amount,
	null as d_general_payment_amount,
	null as s_general_currency_code,
	null as b_is_reorganization,
	null as s_reorganization_info,
	null as d_customer_capital,
	null as t_customer_reg_date,
	null as s_shipping_country_code,
	null as s_shipper_country_code,
	null as s_port_country_code,
	null as s_ship_name,
	null as s_third_country_code,
	null as s_third_bank_country_code,
	s_primesh as s_comment
from
    ccdea_pd_sp
;
